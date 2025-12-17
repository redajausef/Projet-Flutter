import 'package:dio/dio.dart';

import '../models/patient_stats_model.dart';

class PatientRepository {
  final String baseUrl = 'http://localhost:8080/api';
  final Dio dio;

  PatientRepository({Dio? dio})
      : dio = dio ?? Dio(BaseOptions(
              baseUrl: 'http://localhost:8080/api',
              connectTimeout: const Duration(seconds: 30),
              receiveTimeout: const Duration(seconds: 30),
            ));

  Future<Map<String, dynamic>> getPatientById(int userId, String? token) async {
    try {
      // Use /patients/user/{userId} endpoint to get patient by user ID
      final response = await dio.get(
        '/patients/user/$userId',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de récupération du patient: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<PatientStatsModel> getPatientStats(int userId, String? token) async {
    try {
      final patientData = await getPatientById(userId, token);
      return PatientStatsModel.fromPatientJson(patientData);
    } catch (e) {
      throw Exception('Erreur de récupération des statistiques: $e');
    }
  }
}
