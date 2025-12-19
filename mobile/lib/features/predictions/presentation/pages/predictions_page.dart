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
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Analyses Prédictives',
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Prédictions ML scikit-learn',
                          style: TextStyle(
                            color: AppColors.textMuted,
                            fontSize: 13,
                          ),
                        ),
                      ],
                    ),
                    ElevatedButton.icon(
                      onPressed: _generatePrediction,
                      icon: const Icon(Icons.auto_awesome, size: 18),
                      label: const Text('Générer'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.accent,
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
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
                      
                      return ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 24),
                        itemCount: state.predictions.length,
                        itemBuilder: (context, index) {
                          return _PredictionCard(prediction: state.predictions[index]);
                        },
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
    final recommendations = prediction['recommendations'] ?? '';

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
