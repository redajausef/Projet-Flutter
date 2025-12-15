import '../repositories/auth_repository.dart';

class LoginUseCase {
  final AuthRepository repository;

  LoginUseCase(this.repository);

  Future<AuthResult> call(String usernameOrEmail, String password) {
    return repository.login(usernameOrEmail, password);
  }
}

