import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_local_datasource.dart';
import '../datasources/auth_remote_datasource.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource _remoteDataSource;
  final AuthLocalDataSource _localDataSource;

  AuthRepositoryImpl(this._remoteDataSource, this._localDataSource);

  @override
  Future<AuthResult> login(String usernameOrEmail, String password) async {
    final result = await _remoteDataSource.login(usernameOrEmail, password);
    await _localDataSource.saveTokens(result.accessToken, result.refreshToken);
    await _localDataSource.saveUser(result.user);
    return result;
  }

  @override
  Future<AuthResult> register({
    required String username,
    required String email,
    required String password,
    required String firstName,
    required String lastName,
  }) async {
    final result = await _remoteDataSource.register(
      username: username,
      email: email,
      password: password,
      firstName: firstName,
      lastName: lastName,
    );
    await _localDataSource.saveTokens(result.accessToken, result.refreshToken);
    await _localDataSource.saveUser(result.user);
    return result;
  }

  @override
  Future<void> logout() async {
    await _localDataSource.clearAll();
  }

  @override
  Future<bool> isLoggedIn() async {
    return await _localDataSource.hasToken();
  }

  @override
  Future<User?> getCurrentUser() async {
    return _localDataSource.getUser();
  }

  @override
  Future<void> refreshToken() async {
    final refreshToken = await _localDataSource.getRefreshToken();
    if (refreshToken != null) {
      final result = await _remoteDataSource.refreshToken(refreshToken);
      await _localDataSource.saveTokens(result.accessToken, result.refreshToken);
    }
  }
}

