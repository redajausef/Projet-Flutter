import 'package:dio/dio.dart';

class SeanceRepository {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8080/api',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  Future<List<Map<String, dynamic>>> getPatientSeances(int patientId, String token) async {
    _dio.options.headers['Authorization'] = 'Bearer $token';
    
    try {
      final response = await _dio.get('/seances/patient/$patientId');
      final data = response.data as List;
      return data.map((s) => s as Map<String, dynamic>).toList();
    } on DioException catch (e) {
      throw Exception('Erreur: ${e.message}');
    }
  }

  Future<Map<String, dynamic>> createSeance(Map<String, dynamic> seanceData, String token) async {
    _dio.options.headers['Authorization'] = 'Bearer $token';
    
    try {
      final response = await _dio.post('/seances', data: seanceData);
      return response.data;
    } on DioException catch (e) {
      throw Exception('Erreur: ${e.message}');
    }
  }
}
