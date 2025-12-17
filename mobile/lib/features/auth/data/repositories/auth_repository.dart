import 'package:dio/dio.dart';

import '../models/therapeute_model.dart';

class AuthRepository {
  final String baseUrl = 'http://localhost:8080/api'; // Use localhost for web/chrome
  final Dio dio;

  AuthRepository({Dio? dio})
      : dio = dio ?? Dio(BaseOptions(
              baseUrl: 'http://localhost:8080/api',
              connectTimeout: const Duration(seconds: 30),
              receiveTimeout: const Duration(seconds: 30),
            ));

  Future<Map<String, dynamic>> login({
    required String username,
    required String password,
  }) async {
    try {
      final response = await dio.post(
        '/auth/login',
        data: {
          'usernameOrEmail': username,
          'password': password,
        },
      );

      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Échec de connexion: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    } catch (e) {
      throw Exception('Erreur de connexion: $e');
    }
  }

  Future<Map<String, dynamic>> registerPatient({
    required String email,
    required String firstName,
    required String lastName,
    required String phoneNumber,
    required DateTime dateOfBirth,
    required String gender,
    required String password,
    String? address,
    int? assignedTherapeuteId,
  }) async {
    try {
      final response = await dio.post(
        '/auth/register/patient',
        data: {
          'email': email,
          'firstName': firstName,
          'lastName': lastName,
          'phoneNumber': phoneNumber,
          'dateOfBirth': dateOfBirth.toIso8601String().split('T')[0],
          'gender': gender,
          'address': address,
          'assignedTherapeuteId': assignedTherapeuteId,
        },
      );

      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      if (e.response != null) {
        final error = e.response?.data;
        throw Exception(error['message'] ?? 'Échec de l\'inscription');
      } else {
        throw Exception('Erreur d\'inscription: ${e.message}');
      }
    } catch (e) {
      throw Exception('Erreur d\'inscription: $e');
    }
  }

  Future<List<TherapeuteModel>> getAvailableTherapeutes() async {
    try {
      final response = await dio.get('/therapeutes');

      // Backend returns paginated response with 'content' field
      final Map<String, dynamic> paginatedData = response.data as Map<String, dynamic>;
      final List<dynamic> data = paginatedData['content'] as List;
      
      return data
          .map((json) => TherapeuteModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Échec de récupération des thérapeutes: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de récupération des thérapeutes: ${e.message}');
      }
    } catch (e) {
      throw Exception('Erreur de récupération des thérapeutes: $e');
    }
  }

  Future<void> logout() async {
    // Clear local storage/secure storage
    // No API call needed for JWT logout
  }
}
