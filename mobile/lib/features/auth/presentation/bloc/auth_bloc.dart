import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../data/repositories/auth_repository.dart';

// Events
abstract class AuthEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class CheckAuthStatus extends AuthEvent {}

class LoginRequested extends AuthEvent {
  final String usernameOrEmail;
  final String password;
  
  LoginRequested({required this.usernameOrEmail, required this.password});
  
  @override
  List<Object?> get props => [usernameOrEmail, password];
}

class LogoutRequested extends AuthEvent {}

// States
abstract class AuthState extends Equatable {
  @override
  List<Object?> get props => [];
}

class AuthInitial extends AuthState {}

class AuthLoading extends AuthState {}

class Authenticated extends AuthState {
  final String token;
  final String username;
  final int userId;
  
  Authenticated({required this.token, required this.username, required this.userId});
  
  @override
  List<Object?> get props => [token, username, userId];
}

class Unauthenticated extends AuthState {}

class AuthError extends AuthState {
  final String message;
  
  AuthError(this.message);
  
  @override
  List<Object?> get props => [message];
}

// Bloc
class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository authRepository;

  AuthBloc({required this.authRepository}) : super(AuthInitial()) {
    on<CheckAuthStatus>(_onCheckAuthStatus);
    on<LoginRequested>(_onLoginRequested);
    on<LogoutRequested>(_onLogoutRequested);
  }

  Future<void> _onCheckAuthStatus(CheckAuthStatus event, Emitter<AuthState> emit) async {
    final token = await authRepository.getToken();
    if (token != null && token.isNotEmpty) {
      final userId = await authRepository.getUserId();
      emit(Authenticated(token: token, username: '', userId: userId ?? 0));
    } else {
      emit(Unauthenticated());
    }
  }

  Future<void> _onLoginRequested(LoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final result = await authRepository.login(event.usernameOrEmail, event.password);
      final token = result['accessToken'] as String? ?? '';
      final user = result['user'] as Map<String, dynamic>?;
      final username = user?['username'] as String? ?? '';
      final userId = user?['id'] as int? ?? 0;
      
      emit(Authenticated(token: token, username: username, userId: userId));
    } catch (e) {
      emit(AuthError(e.toString().replaceAll('Exception: ', '')));
    }
  }

  Future<void> _onLogoutRequested(LogoutRequested event, Emitter<AuthState> emit) async {
    await authRepository.logout();
    emit(Unauthenticated());
  }
}
