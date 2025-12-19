import 'package:dio/dio.dart';

import '../models/prediction_model.dart';

class PredictionRepository {
  // Use 10.0.2.2 for Android emulator (maps to host's localhost)
  final String baseUrl = 'http://localhost:8080/api';
  final Dio dio;

  PredictionRepository({Dio? dio})
      : dio = dio ?? Dio(BaseOptions(
              baseUrl: 'http://localhost:8080/api',
              connectTimeout: const Duration(seconds: 30),
              receiveTimeout: const Duration(seconds: 30),
            ));

  Future<List<PredictionModel>> getPatientPredictions(int patientId, String? token) async {
    try {
      final response = await dio.get(
        '/predictions/patient/$patientId',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      final List<dynamic> data = response.data as List;
      return data.map((json) => PredictionModel.fromJson(json as Map<String, dynamic>)).toList();
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de récupération des prédictions: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<PredictionModel> generateNextSessionPrediction(int patientId, String? token) async {
    try {
      final response = await dio.post(
        '/predictions/patient/$patientId/next-session',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      return PredictionModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de génération de prédiction: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }

  Future<PredictionModel> generateDropoutRiskPrediction(int patientId, String? token) async {
    try {
      final response = await dio.post(
        '/predictions/patient/$patientId/dropout-risk',
        options: Options(
          headers: token != null ? {'Authorization': 'Bearer $token'} : null,
        ),
      );

      return PredictionModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      if (e.response != null) {
        throw Exception('Erreur de génération de prédiction: ${e.response?.statusCode}');
      } else {
        throw Exception('Erreur de connexion: ${e.message}');
      }
    }
  }
}
