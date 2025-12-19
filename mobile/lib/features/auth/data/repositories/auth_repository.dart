import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthRepository {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8080/api',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  Future<Map<String, dynamic>> login(String usernameOrEmail, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'usernameOrEmail': usernameOrEmail,
        'password': password,
      });
      
      final data = response.data;
      
      // Save token and user info
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', data['accessToken'] ?? '');
      await prefs.setInt('user_id', data['user']?['id'] ?? 0);
      await prefs.setString('username', data['user']?['username'] ?? '');
      await prefs.setString('email', data['user']?['email'] ?? '');
      await prefs.setString('role', data['user']?['role'] ?? '');
      
      return data;
    } on DioException catch (e) {
      throw Exception(_handleDioError(e));
    }
  }

  Future<Map<String, dynamic>> register(Map<String, dynamic> userData) async {
    try {
      final response = await _dio.post('/auth/register', data: userData);
      return response.data;
    } on DioException catch (e) {
      throw Exception(_handleDioError(e));
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token');
  }

  Future<int?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt('user_id');
  }

  Future<int?> getPatientId() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getInt('user_id');
    final token = prefs.getString('token');
    
    if (userId == null || token == null) return null;
    
    try {
      final response = await _dio.get(
        '/patients/user/$userId',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      return response.data['id'] as int?;
    } catch (e) {
      return null;
    }
  }

  String _handleDioError(DioException e) {
    if (e.response?.statusCode == 401) {
      return 'Identifiants incorrects';
    } else if (e.response?.statusCode == 403) {
      return 'Accès refusé';
    } else if (e.type == DioExceptionType.connectionTimeout) {
      return 'Connexion au serveur impossible';
    }
    return e.message ?? 'Erreur de connexion';
  }
}
