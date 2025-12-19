import 'package:dio/dio.dart';

class PredictionRepository {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8080/api',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  Future<List<Map<String, dynamic>>> getPatientPredictions(int patientId, String token) async {
    _dio.options.headers['Authorization'] = 'Bearer $token';
    
    try {
      final response = await _dio.get('/predictions/patient/$patientId');
      final data = response.data as List;
      return data.map((p) => p as Map<String, dynamic>).toList();
    } on DioException catch (e) {
      throw Exception('Erreur: ${e.message}');
    }
  }

  Future<Map<String, dynamic>> generatePrediction(int patientId, String token) async {
    _dio.options.headers['Authorization'] = 'Bearer $token';
    
    try {
      final response = await _dio.post('/predictions/generate/dropout-risk/$patientId');
      return response.data;
    } on DioException catch (e) {
      throw Exception('Erreur: ${e.message}');
    }
  }
}
