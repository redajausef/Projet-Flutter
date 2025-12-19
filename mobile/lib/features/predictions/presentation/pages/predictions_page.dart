import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../data/models/prediction_model.dart';
import '../bloc/prediction_bloc.dart';
import '../bloc/prediction_event.dart';
import '../bloc/prediction_state.dart';


class PredictionsPage extends StatefulWidget {
  const PredictionsPage({super.key});

  @override
  State<PredictionsPage> createState() => _PredictionsPageState();
}

class _PredictionsPageState extends State<PredictionsPage> {
  int _patientId = 1; // Default patient ID for demo

  @override
  void initState() {
    super.initState();
    _loadPatientIdAndPredictions();
  }

  Future<void> _loadPatientIdAndPredictions() async {
    // For demo, always use patientId 1 (Sara Ouazzani - good patient)
    // Note: In production, this should fetch the patient associated with the user
    _patientId = 1;
    _loadPredictions();
  }

  void _loadPredictions() {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated) {
      context.read<PredictionBloc>().add(
        LoadPatientPredictions(
          _patientId,
          authState.token,
        ),
      );
    }
  }

  void _generatePrediction() {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated) {
      context.read<PredictionBloc>().add(
        GenerateDropoutRiskPrediction(
          _patientId,
          authState.token,
        ),
      );
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
          child: RefreshIndicator(
            onRefresh: () async => _loadPredictions(),
            child: BlocConsumer<PredictionBloc, PredictionState>(
              listener: (context, state) {
                if (state is PredictionGenerated) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Row(
                        children: [
                          const Icon(Icons.check_circle, color: Colors.white),
                          const SizedBox(width: 8),
                          Text(
                            'Prédiction ML générée: ${state.prediction.score.round()}% (${state.prediction.algorithm ?? "ML"})',
                          ),
                        ],
                      ),
                      backgroundColor: AppColors.success,
                    ),
                  );
                  // Reload predictions after generation
                  _loadPredictions();
                } else if (state is PredictionError) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text(state.message),
                      backgroundColor: AppColors.error,
                    ),
                  );
                }
              },
              builder: (context, state) {
                return CustomScrollView(
                  slivers: [
                    // Header with Generate Button
                    SliverToBoxAdapter(
                      child: _buildHeader(),
                    ),
                    // Main Content
                    if (state is PredictionLoading)
                      const SliverFillRemaining(
                        child: Center(
                          child: CircularProgressIndicator(
                            color: AppColors.accent,
                          ),
                        ),
                      )
                    else if (state is PredictionsLoaded)
                      ...[
                        // Risk Score Card
                        SliverToBoxAdapter(
                          child: _buildRiskScoreCard(state.predictions),
                        ),
                        // ML Algorithm Info
                        SliverToBoxAdapter(
                          child: _buildMLInfoCard(state.predictions),
                        ),
                        // Predictions List
                        SliverToBoxAdapter(
                          child: _buildPredictionsList(state.predictions),
                        ),
                        // Factors Card
                        SliverToBoxAdapter(
                          child: _buildFactorsCard(state.predictions),
                        ),
                        // Recommendations
                        SliverToBoxAdapter(
                          child: _buildRecommendations(state.predictions),
                        ),
                      ]
                    else if (state is PredictionError)
                      SliverFillRemaining(
                        child: Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.error_outline, 
                                color: AppColors.error, 
                                size: 48),
                              const SizedBox(height: 16),
                              Text(
                                'Erreur de chargement',
                                style: TextStyle(
                                  color: AppColors.textPrimary,
                                  fontSize: 18,
                                ),
                              ),
                              const SizedBox(height: 8),
                              ElevatedButton(
                                onPressed: _loadPredictions,
                                child: const Text('Réessayer'),
                              ),
                            ],
                          ),
                        ),
                      )
                    else
                      SliverFillRemaining(
                        child: Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.psychology_outlined,
                                color: AppColors.textMuted,
                                size: 64),
                              const SizedBox(height: 16),
                              const Text(
                                'Aucune prédiction',
                                style: TextStyle(
                                  color: AppColors.textPrimary,
                                  fontSize: 18,
                                ),
                              ),
                              const SizedBox(height: 8),
                              ElevatedButton.icon(
                                onPressed: _generatePrediction,
                                icon: const Icon(Icons.auto_awesome),
                                label: const Text('Générer maintenant'),
                              ),
                            ],
                          ),
                        ),
                      ),
                    const SliverToBoxAdapter(
                      child: SizedBox(height: 100),
                    ),
                  ],
                );
              },
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
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
                  color: AppColors.textPrimary,
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Prédictions ML scikit-learn',
                style: TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 14,
                ),
              ),
            ],
          ),
          ElevatedButton.icon(
            onPressed: _generatePrediction,
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.accent,
              foregroundColor: Colors.black,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            ),
            icon: const Icon(Icons.auto_awesome, size: 18),
            label: const Text('Générer IA'),
          ),
        ],
      ),
    );
  }

  Widget _buildRiskScoreCard(List<PredictionModel> predictions) {
    final dropoutPred = predictions.firstWhere(
      (p) => p.type == 'DROPOUT_RISK',
      orElse: () => predictions.isNotEmpty ? predictions.first : _emptyPrediction(),
    );

    final score = dropoutPred.score.round();
    final color = _getRiskColor(dropoutPred.riskLevel);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              AppColors.card,
              color.withOpacity(0.2),
            ],
          ),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(
            color: color.withOpacity(0.3),
          ),
        ),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Score de Risque',
                      style: TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          '$score',
                          style: const TextStyle(
                            color: AppColors.textPrimary,
                            fontSize: 48,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const Padding(
                          padding: EdgeInsets.only(bottom: 10),
                          child: Text(
                            '%',
                            style: TextStyle(
                              color: AppColors.textMuted,
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: LinearGradient(
                      colors: [
                        color.withOpacity(0.3),
                        color.withOpacity(0.1),
                      ],
                    ),
                    border: Border.all(
                      color: color,
                      width: 3,
                    ),
                  ),
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          _getRiskIcon(dropoutPred.riskLevel),
                          color: color,
                          size: 28,
                        ),
                        Text(
                          dropoutPred.riskCategoryDisplay,
                          style: TextStyle(
                            color: color,
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            // Confidence Bar
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Confiance du modèle',
                        style: TextStyle(
                          color: AppColors.textMuted,
                          fontSize: 11,
                        ),
                      ),
                      const SizedBox(height: 6),
                      ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          value: dropoutPred.confidence,
                          backgroundColor: AppColors.surfaceLight,
                          valueColor: AlwaysStoppedAnimation<Color>(AppColors.accent),
                          minHeight: 6,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${(dropoutPred.confidence * 100).round()}%',
                        style: TextStyle(
                          color: AppColors.accent,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMLInfoCard(List<PredictionModel> predictions) {
    if (predictions.isEmpty) return const SizedBox.shrink();
    
    final pred = predictions.first;
    final isRealML = pred.isRealML;

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isRealML 
            ? AppColors.success.withOpacity(0.1) 
            : AppColors.warning.withOpacity(0.1),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isRealML 
              ? AppColors.success.withOpacity(0.3) 
              : AppColors.warning.withOpacity(0.3),
          ),
        ),
        child: Row(
          children: [
            Icon(
              isRealML ? Icons.psychology : Icons.calculate,
              color: isRealML ? AppColors.success : AppColors.warning,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Algorithme: ${pred.algorithm ?? "ML"}',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  if (pred.modelVersion != null)
                    Text(
                      'Version: ${pred.modelVersion}',
                      style: TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 12,
                      ),
                    ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(
                color: isRealML 
                  ? AppColors.success.withOpacity(0.2)
                  : AppColors.warning.withOpacity(0.2),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                isRealML ? 'scikit-learn' : 'Fallback',
                style: TextStyle(
                  color: isRealML ? AppColors.success : AppColors.warning,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPredictionsList(List<PredictionModel> predictions) {
    if (predictions.isEmpty) {
      return const SizedBox.shrink();
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Prédictions récentes',
            style: TextStyle(
              color: AppColors.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 16),
          ...predictions.take(3).map((pred) => Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: _buildPredictionCard(pred),
          )),
        ],
      ),
    );
  }

  Widget _buildPredictionCard(PredictionModel pred) {
    final color = _getRiskColor(pred.riskLevel);
    final icon = _getPredictionIcon(pred.type);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: color.withOpacity(0.15),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: color),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _getPredictionTitle(pred.type),
                  style: TextStyle(
                    color: AppColors.textSecondary,
                    fontSize: 12,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${pred.score.round()}% - ${pred.riskCategoryDisplay}',
                  style: const TextStyle(
                    color: AppColors.textPrimary,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(
              color: AppColors.surfaceLight,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              '${(pred.confidence * 100).round()}%',
              style: TextStyle(
                color: AppColors.accent,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFactorsCard(List<PredictionModel> predictions) {
    final pred = predictions.firstWhere(
      (p) => p.factors != null && p.factors!.isNotEmpty,
      orElse: () => _emptyPrediction(),
    );

    if (pred.factors == null || pred.factors!.isEmpty) {
      return const SizedBox.shrink();
    }

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.analytics, color: AppColors.accent),
                const SizedBox(width: 8),
                const Text(
                  'Facteurs ML',
                  style: TextStyle(
                    color: AppColors.textPrimary,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: pred.factors!.entries.map((entry) {
                return Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: AppColors.surfaceLight,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    '${PredictionModel.getFactorLabel(entry.key)}: ${PredictionModel.formatFactorValue(entry.key, entry.value)}',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 12,
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecommendations(List<PredictionModel> predictions) {
    final pred = predictions.firstWhere(
      (p) => p.recommendation != null && p.recommendation!.isNotEmpty,
      orElse: () => _emptyPrediction(),
    );

    final recommendation = pred.recommendation ?? 
      'Continuez vos exercices et maintenez votre suivi régulier.';

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              AppColors.accent.withOpacity(0.15),
              AppColors.accent.withOpacity(0.05),
            ],
          ),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: AppColors.accent.withOpacity(0.3),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: AppColors.accent.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(
                    Icons.auto_awesome_rounded,
                    color: AppColors.accent,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                const Text(
                  'Recommandation IA',
                  style: TextStyle(
                    color: AppColors.accent,
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              recommendation,
              style: TextStyle(
                color: AppColors.textSecondary,
                fontSize: 14,
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Helper methods
  Color _getRiskColor(String riskLevel) {
    switch (riskLevel.toUpperCase()) {
      case 'LOW':
        return AppColors.success;
      case 'MODERATE':
        return AppColors.warning;
      case 'HIGH':
        return AppColors.error;
      case 'CRITICAL':
        return Colors.red.shade900;
      default:
        return AppColors.info;
    }
  }

  IconData _getRiskIcon(String riskLevel) {
    switch (riskLevel.toUpperCase()) {
      case 'LOW':
        return Icons.shield_rounded;
      case 'MODERATE':
        return Icons.warning_rounded;
      case 'HIGH':
        return Icons.dangerous_rounded;
      case 'CRITICAL':
        return Icons.error_rounded;
      default:
        return Icons.help_rounded;
    }
  }

  IconData _getPredictionIcon(String type) {
    switch (type) {
      case 'DROPOUT_RISK':
        return Icons.psychology_rounded;
      case 'NEXT_SESSION':
        return Icons.event_available_rounded;
      case 'TREATMENT_OUTCOME':
        return Icons.trending_up_rounded;
      default:
        return Icons.analytics_rounded;
    }
  }

  String _getPredictionTitle(String type) {
    switch (type) {
      case 'DROPOUT_RISK':
        return "Risque d'abandon";
      case 'NEXT_SESSION':
        return 'Prochaine séance';
      case 'TREATMENT_OUTCOME':
        return 'Progrès du traitement';
      default:
        return 'Prédiction';
    }
  }

  PredictionModel _emptyPrediction() {
    return PredictionModel(
      id: 0,
      patientId: 0,
      type: 'DROPOUT_RISK',
      score: 0,
      confidence: 0,
      riskLevel: 'LOW',
      generatedAt: DateTime.now(),
      isActive: true,
    );
  }
}
