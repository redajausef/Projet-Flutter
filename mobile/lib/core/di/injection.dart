import 'package:get_it/get_it.dart';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../features/auth/data/datasources/auth_local_datasource.dart';
import '../../features/auth/data/datasources/auth_remote_datasource.dart';
import '../../features/auth/data/repositories/auth_repository_impl.dart';
import '../../features/auth/domain/repositories/auth_repository.dart';
import '../../features/auth/domain/usecases/login_usecase.dart';
import '../../features/auth/presentation/bloc/auth_bloc.dart';
import '../network/api_client.dart';

final getIt = GetIt.instance;

Future<void> initializeDependencies() async {
  // External
  final sharedPreferences = await SharedPreferences.getInstance();
  getIt.registerSingleton<SharedPreferences>(sharedPreferences);
  
  const secureStorage = FlutterSecureStorage();
  getIt.registerSingleton<FlutterSecureStorage>(secureStorage);

  // Network
  getIt.registerSingleton<Dio>(createDio());
  getIt.registerSingleton<ApiClient>(ApiClient(getIt<Dio>()));

  // Data sources
  getIt.registerSingleton<AuthLocalDataSource>(
    AuthLocalDataSource(getIt<FlutterSecureStorage>(), getIt<SharedPreferences>()),
  );
  getIt.registerSingleton<AuthRemoteDataSource>(
    AuthRemoteDataSource(getIt<ApiClient>()),
  );

  // Repositories
  getIt.registerSingleton<AuthRepository>(
    AuthRepositoryImpl(getIt<AuthRemoteDataSource>(), getIt<AuthLocalDataSource>()),
  );

  // Use cases
  getIt.registerFactory(() => LoginUseCase(getIt<AuthRepository>()));

  // Blocs
  getIt.registerFactory(() => AuthBloc(
    loginUseCase: getIt<LoginUseCase>(),
    authLocalDataSource: getIt<AuthLocalDataSource>(),
  ));
}

Dio createDio() {
  final dio = Dio(BaseOptions(
    baseUrl: 'http://10.0.2.2:8080/api', // Android emulator localhost
    connectTimeout: const Duration(seconds: 30),
    receiveTimeout: const Duration(seconds: 30),
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
  ));

  dio.interceptors.add(LogInterceptor(
    requestBody: true,
    responseBody: true,
  ));

  return dio;
}

