/// Model for ML Predictions from backend
/// Supports RandomForest, GradientBoosting, and other scikit-learn algorithms
class PredictionModel {
  final int id;
  final int patientId;
  final String? patientName;
  final String type;
  final double score;
  final double confidence;
  final String riskLevel;
  final String? recommendation;
  final Map<String, double>? factors;  // Changed to Map for factor values
  final DateTime generatedAt;
  final bool isActive;
  final String? algorithm;      // ML algorithm: RandomForest, GradientBoosting, etc.
  final String? modelVersion;   // Model version: 1.0.0

  PredictionModel({
    required this.id,
    required this.patientId,
    this.patientName,
    required this.type,
    required this.score,
    required this.confidence,
    required this.riskLevel,
    this.recommendation,
    this.factors,
    required this.generatedAt,
    required this.isActive,
    this.algorithm,
    this.modelVersion,
  });

  /// Parse risk category for display
  String get riskCategoryDisplay {
    switch (riskLevel.toUpperCase()) {
      case 'LOW':
        return 'Faible';
      case 'MODERATE':
        return 'Modéré';
      case 'HIGH':
        return 'Élevé';
      case 'CRITICAL':
        return 'Critique';
      default:
        return riskLevel;
    }
  }

  /// Check if using real ML (not heuristic fallback)
  bool get isRealML => algorithm != null && algorithm != 'HeuristicFallback';

  factory PredictionModel.fromJson(Map<String, dynamic> json) {
    // Parse factors - can be List<String> or Map<String, dynamic>
    Map<String, double>? parsedFactors;
    if (json['factors'] != null) {
      if (json['factors'] is Map) {
        parsedFactors = (json['factors'] as Map<String, dynamic>).map(
          (key, value) => MapEntry(key, (value as num).toDouble()),
        );
      } else if (json['factors'] is List) {
        // Convert old List format to Map with default values
        parsedFactors = {};
        for (var f in json['factors'] as List) {
          parsedFactors[f.toString()] = 0.0;
        }
      }
    }

    return PredictionModel(
      id: json['id'] as int,
      patientId: json['patientId'] as int,
      patientName: json['patientName'] as String?,
      type: json['type'] as String,
      score: (json['score'] as num).toDouble(),
      confidence: (json['confidence'] as num?)?.toDouble() ?? 0.85,
      riskLevel: json['riskLevel'] as String? ?? 'LOW',
      recommendation: json['recommendation'] as String?,
      factors: parsedFactors,
      generatedAt: json['generatedAt'] != null 
          ? DateTime.parse(json['generatedAt'] as String)
          : DateTime.now(),
      isActive: json['isActive'] as bool? ?? true,
      algorithm: json['algorithm'] as String? ?? json['algorithmUsed'] as String?,
      modelVersion: json['modelVersion'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'patientName': patientName,
      'type': type,
      'score': score,
      'confidence': confidence,
      'riskLevel': riskLevel,
      'recommendation': recommendation,
      'factors': factors,
      'generatedAt': generatedAt.toIso8601String(),
      'isActive': isActive,
      'algorithm': algorithm,
      'modelVersion': modelVersion,
    };
  }

  /// Get translated factor name
  static String getFactorLabel(String factorKey) {
    const translations = {
      'days_since_last_session': 'Jours depuis dernière séance',
      'no_show_rate': "Taux d'absence",
      'cancellation_rate': "Taux d'annulation",
      'cancellation_impact': 'Impact annulations',
      'no_show_impact': 'Impact absences',
      'inactivity_impact': 'Impact inactivité',
      'total_sessions': 'Nombre de séances',
      'avg_progress_rating': 'Note de progression',
      'mood_improvement': 'Amélioration humeur',
    };
    return translations[factorKey] ?? 
           factorKey.replaceAll('_', ' ').split(' ').map((w) => 
             w.isNotEmpty ? w[0].toUpperCase() + w.substring(1) : w
           ).join(' ');
  }

  /// Format factor value with appropriate suffix
  static String formatFactorValue(String key, double value) {
    if (key.contains('rate') || key.contains('impact')) {
      return '${value.round()}%';
    } else if (key == 'days_since_last_session') {
      return '${value.round()}j';
    }
    return value.toStringAsFixed(1);
  }
}
