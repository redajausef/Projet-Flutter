import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PatientRepository {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8080/api',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  Future<Map<String, dynamic>> getPatientStats(String token) async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getInt('user_id');
    
    if (userId == null) throw Exception('User ID not found');
    
    _dio.options.headers['Authorization'] = 'Bearer $token';
    
    try {
      // Get patient by user ID
      final patientResponse = await _dio.get('/patients/user/$userId');
      final patientId = patientResponse.data['id'] as int;
      
      // Get patient séances
      final seancesResponse = await _dio.get('/seances/patient/$patientId');
      final seances = seancesResponse.data as List;
      
      final completed = seances.where((s) => s['status'] == 'COMPLETED').length;
      final upcoming = seances.where((s) => s['status'] == 'SCHEDULED' || s['status'] == 'CONFIRMED').length;
      
      // Get patient info
      final patient = patientResponse.data;
      
      return {
        'patientId': patientId,
        'patientName': patient['user']?['prenom'] ?? patient['user']?['username'] ?? 'Patient',
        'totalSeances': seances.length,
        'completedSeances': completed,
        'upcomingSeances': upcoming,
        'progressPercent': seances.isEmpty ? 0 : ((completed / seances.length) * 100).round(),
        'riskScore': patient['riskScore'] ?? 0,
        'riskCategory': patient['riskCategory'] ?? 'LOW',
        'upcomingAppointments': seances.where((s) => 
          s['status'] == 'SCHEDULED' || s['status'] == 'CONFIRMED'
        ).take(3).toList(),
      };
    } on DioException catch (e) {
      throw Exception('Erreur de chargement: ${e.message}');
    }
  }
}
