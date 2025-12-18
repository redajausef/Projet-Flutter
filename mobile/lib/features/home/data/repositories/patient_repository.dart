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
      // Get patient data
      final patientData = await getPatientById(userId, token);
      final patientId = patientData['id'] as int;
      
      print('Loading stats for patient_id: $patientId (user_id: $userId)');
      
      // Get patient seances to calculate real stats
      final seancesResponse = await dio.get(
        '/seances/patient/$patientId',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );
      
      final List<dynamic> seancesData = seancesResponse.data as List;
      final now = DateTime.now();
      
      print('Total seances fetched: ${seancesData.length}');
      
      // Calculate stats from actual seances
      int totalSeances = seancesData.length;
      int completedSeances = seancesData.where((s) => s['status'] == 'COMPLETED').length;
      int upcomingSeances = seancesData.where((s) {
        final scheduledAt = DateTime.parse(s['scheduledAt']);
        final status = s['status'];
        return scheduledAt.isAfter(now) && 
               (status == 'SCHEDULED' || status == 'CONFIRMED' || status == 'PENDING_APPROVAL');
      }).length;
      
      print('Stats calculated - Total: $totalSeances, Completed: $completedSeances, Upcoming: $upcomingSeances');
      
      return PatientStatsModel(
        totalSeances: totalSeances,
        completedSeances: completedSeances,
        upcomingSeances: upcomingSeances,
        riskScore: patientData['riskScore'] != null ? (patientData['riskScore'] as num).toDouble() : null,
        riskCategory: patientData['riskCategory'] as String?,
        progressPercentage: totalSeances > 0 ? (completedSeances / totalSeances * 100) : 0,
      );
    } catch (e) {
      print('Error loading patient stats: $e');
      throw Exception('Erreur de récupération des statistiques: $e');
    }
  }
}
