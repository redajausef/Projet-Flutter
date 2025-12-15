import '../../../../core/network/api_client.dart';
import '../../domain/repositories/auth_repository.dart';

class AuthRemoteDataSource {
  final ApiClient _apiClient;

  AuthRemoteDataSource(this._apiClient);

  Future<AuthResult> login(String usernameOrEmail, String password) async {
    final response = await _apiClient.post(
      '/auth/login',
      data: {
        'usernameOrEmail': usernameOrEmail,
        'password': password,
      },
    );
    return AuthResult.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AuthResult> register({
    required String username,
    required String email,
    required String password,
    required String firstName,
    required String lastName,
  }) async {
    final response = await _apiClient.post(
      '/auth/register',
      data: {
        'username': username,
        'email': email,
        'password': password,
        'firstName': firstName,
        'lastName': lastName,
        'role': 'PATIENT',
      },
    );
    return AuthResult.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AuthResult> refreshToken(String refreshToken) async {
    final response = await _apiClient.post('/auth/refresh');
    return AuthResult.fromJson(response.data as Map<String, dynamic>);
  }
}

