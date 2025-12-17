class PredictionModel {
  final int id;
  final int patientId;
  final String? patientName;
  final String type;
  final double score;
  final double confidence;
  final String riskLevel;
  final String? recommendation;
  final List<String>? factors;
  final DateTime generatedAt;
  final bool isActive;

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
  });

  factory PredictionModel.fromJson(Map<String, dynamic> json) {
    return PredictionModel(
      id: json['id'] as int,
      patientId: json['patientId'] as int,
      patientName: json['patientName'] as String?,
      type: json['type'] as String,
      score: (json['score'] as num).toDouble(),
      confidence: (json['confidence'] as num).toDouble(),
      riskLevel: json['riskLevel'] as String,
      recommendation: json['recommendation'] as String?,
      factors: json['factors'] != null 
          ? List<String>.from(json['factors'] as List)
          : null,
      generatedAt: DateTime.parse(json['generatedAt'] as String),
      isActive: json['isActive'] as bool? ?? true,
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
    };
  }
}
