import 'package:dio/dio.dart';

import '../models/seance_model.dart';
import '../models/create_seance_request.dart';

class SeanceRepository {
  final String baseUrl = 'http://localhost:8080/api';
  final Dio dio;

  SeanceRepository({Dio? dio})
      : dio = dio ?? Dio(BaseOptions(
              baseUrl: 'http://localhost:8080/api',
              connectTimeout: const Duration(seconds: 30),
              receiveTimeout: const Duration(seconds: 30),
            ));

  Future<List<SeanceModel>> getPatientSeances(int patientId, String? token) async {
    try {
      final response = await dio.get(
        '/seances/patient/$patientId',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      final List<dynamic> data = response.data as List;
      return data.map((json) => SeanceModel.fromJson(json as Map<String, dynamic>)).toList();
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de récupération des séances: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<List<SeanceModel>> getUpcomingSeances(int patientId, String? token) async {
    try {
      final response = await dio.get(
        '/seances/patient/$patientId',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      final List<dynamic> data = response.data as List;
      final seances = data.map((json) => SeanceModel.fromJson(json as Map<String, dynamic>)).toList();
      
      // Filter upcoming seances (include SCHEDULED, CONFIRMED, PENDING_APPROVAL)
      final now = DateTime.now();
      return seances.where((s) => 
        s.scheduledAt.isAfter(now) && 
        (s.status == 'SCHEDULED' || s.status == 'CONFIRMED' || s.status == 'PENDING_APPROVAL')
      ).toList()
        ..sort((a, b) => a.scheduledAt.compareTo(b.scheduledAt));
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de récupération des séances: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<SeanceModel> createSeance(CreateSeanceRequest request, String token) async {
    try {
      final response = await dio.post(
        '/seances',
        data: request.toJson(),
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );

      return SeanceModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      if (e.response != null) {
        final message = e.response?.data['message'] ?? 'Erreur lors de la création';
        throw Exception('Erreur: $message');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<bool> checkConflict(int therapeuteId, DateTime scheduledAt, int durationMinutes, String token) async {
    try {
      final response = await dio.get(
        '/seances/check-conflict',
        queryParameters: {
          'therapeuteId': therapeuteId,
          'scheduledAt': scheduledAt.toIso8601String(),
          'durationMinutes': durationMinutes,
        },
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );

      return response.data as bool;
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de vérification: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }
}
