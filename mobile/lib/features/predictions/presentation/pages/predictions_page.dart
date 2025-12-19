import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dio/dio.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../bloc/prediction_bloc.dart';

class PredictionsPage extends StatefulWidget {
  const PredictionsPage({super.key});

  @override
  State<PredictionsPage> createState() => _PredictionsPageState();
}

class _PredictionsPageState extends State<PredictionsPage> {
  int _patientId = 0;

  @override
  void initState() {
    super.initState();
    _loadPredictions();
  }

  void _loadPredictions() async {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated) {
      try {
        final prefs = await SharedPreferences.getInstance();
        final userId = prefs.getInt('user_id');
        if (userId == null) return;
        
        final dio = Dio(BaseOptions(
          baseUrl: 'http://localhost:8080/api',
          headers: {'Authorization': 'Bearer ${authState.token}'},
        ));
        
        final patientResponse = await dio.get('/patients/user/$userId');
        _patientId = patientResponse.data['id'] as int;
        
        context.read<PredictionBloc>().add(LoadPatientPredictions(_patientId, authState.token));
      } catch (e) {
        print('Error loading predictions: $e');
      }
    }
  }

  void _generatePrediction() {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated && _patientId > 0) {
      context.read<PredictionBloc>().add(GenerateDropoutRiskPrediction(_patientId, authState.token));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: AppColors.backgroundGradient,
        ),
        child: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(24),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Flexible(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Prédictions ML',
                            style: TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'scikit-learn RandomForest',
                            style: TextStyle(
                              color: AppColors.textMuted,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed: _generatePrediction,
                      icon: const Icon(Icons.auto_awesome, size: 16),
                      label: const Text('ML', style: TextStyle(fontSize: 12)),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.accent,
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      ),
                    ),
                  ],
                ),
              ),
              
              // Predictions content
              Expanded(
                child: BlocBuilder<PredictionBloc, PredictionState>(
                  builder: (context, state) {
                    if (state is PredictionLoading) {
                      return const Center(child: CircularProgressIndicator());
                    }
                    
                    if (state is PredictionError) {
                      return Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(Icons.error_outline, size: 64, color: AppColors.error),
                            const SizedBox(height: 16),
                            Text(state.message),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: _loadPredictions,
                              child: const Text('Réessayer'),
                            ),
                          ],
                        ),
                      );
                    }
                    
                    if (state is PredictionsLoaded) {
                      if (state.predictions.isEmpty) {
                        return Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.analytics, size: 64, color: AppColors.textMuted),
                              const SizedBox(height: 16),
                              Text('Aucune prédiction', style: TextStyle(color: AppColors.textSecondary)),
                              const SizedBox(height: 16),
                              ElevatedButton.icon(
                                onPressed: _generatePrediction,
                                icon: const Icon(Icons.auto_awesome),
                                label: const Text('Générer une prédiction'),
                              ),
                            ],
                          ),
                        );
                      }
                      
                      return SingleChildScrollView(
                        padding: const EdgeInsets.symmetric(horizontal: 24),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Progress Chart
                            if (state.predictions.length > 1)
                              _ProgressChart(predictions: state.predictions),
                            
                            // Predictions list
                            ...state.predictions.map((p) => 
                              _PredictionCard(prediction: p)).toList(),
                          ],
                        ),
                      );
                    }
                    
                    return const SizedBox();
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PredictionCard extends StatelessWidget {
  final Map<String, dynamic> prediction;

  const _PredictionCard({required this.prediction});

  @override
  Widget build(BuildContext context) {
    final riskLevel = prediction['riskLevel'] ?? 0;
    final riskCategory = prediction['riskCategory'] ?? 'LOW';
    final algorithm = prediction['algorithmUsed'] ?? 'RandomForest';
    final confidence = ((prediction['confidenceScore'] ?? 0) * 100).round();
    final recommendations = prediction['patientRecommendations'] ?? prediction['recommendations'] ?? '';

    Color riskColor;
    switch (riskCategory.toString().toUpperCase()) {
      case 'LOW':
        riskColor = AppColors.riskLow;
        break;
      case 'MODERATE':
        riskColor = AppColors.riskModerate;
        break;
      case 'HIGH':
        riskColor = AppColors.riskHigh;
        break;
      case 'CRITICAL':
        riskColor = AppColors.riskCritical;
        break;
      default:
        riskColor = AppColors.riskLow;
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: riskColor.withOpacity(0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with score
          Row(
            children: [
              Container(
                width: 70,
                height: 70,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [riskColor, riskColor.withOpacity(0.7)],
                  ),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Center(
                  child: Text(
                    '$riskLevel%',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Risque d\'abandon',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: riskColor.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        riskCategory,
                        style: TextStyle(
                          color: riskColor,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          
          // Algorithm info
          Row(
            children: [
              _InfoChip(
                icon: Icons.memory,
                label: algorithm,
              ),
              const SizedBox(width: 12),
              _InfoChip(
                icon: Icons.verified,
                label: 'Confiance $confidence%',
              ),
            ],
          ),
          
          if (recommendations.isNotEmpty) ...[
            const SizedBox(height: 16),
            const Text(
              'Recommandations',
              style: TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              recommendations,
              style: TextStyle(
                color: AppColors.textSecondary,
                fontSize: 13,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _InfoChip extends StatelessWidget {
  final IconData icon;
  final String label;

  const _InfoChip({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.surfaceLight,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: AppColors.textMuted),
          const SizedBox(width: 6),
          Text(label, style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
        ],
      ),
    );
  }
}

// Progress Chart Widget showing risk score history
class _ProgressChart extends StatelessWidget {
  final List<Map<String, dynamic>> predictions;

  const _ProgressChart({required this.predictions});

  @override
  Widget build(BuildContext context) {
    // Sort by date and take last 5
    final sorted = List<Map<String, dynamic>>.from(predictions);
    sorted.sort((a, b) {
      final dateA = DateTime.tryParse(a['createdAt']?.toString() ?? '') ?? DateTime.now();
      final dateB = DateTime.tryParse(b['createdAt']?.toString() ?? '') ?? DateTime.now();
      return dateA.compareTo(dateB);
    });
    final chartData = sorted.take(5).toList();
    
    return Container(
      margin: const EdgeInsets.only(bottom: 20),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.accent.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.show_chart, color: AppColors.accent, size: 18),
              ),
              const SizedBox(width: 12),
              const Text(
                'Évolution du risque',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
            ],
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 100,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: chartData.map((p) {
                final riskLevel = (p['riskLevel'] ?? 0) as int;
                final barHeight = (riskLevel / 100) * 80;
                final color = _getBarColor(riskLevel);
                
                return Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        Text(
                          '$riskLevel%',
                          style: TextStyle(fontSize: 10, color: color, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 4),
                        Container(
                          height: barHeight.clamp(10.0, 80.0),
                          decoration: BoxDecoration(
                            color: color,
                            borderRadius: BorderRadius.circular(4),
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          const SizedBox(height: 8),
          Center(
            child: Text(
              'Dernières ${chartData.length} prédictions',
              style: TextStyle(fontSize: 11, color: AppColors.textMuted),
            ),
          ),
        ],
      ),
    );
  }

  Color _getBarColor(int riskLevel) {
    if (riskLevel < 30) return AppColors.success;
    if (riskLevel < 50) return AppColors.warning;
    if (riskLevel < 70) return Colors.orange;
    return AppColors.error;
  }
}
