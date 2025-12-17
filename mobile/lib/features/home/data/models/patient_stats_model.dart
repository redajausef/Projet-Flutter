class PatientStatsModel {
  final int totalSeances;
  final int completedSeances;
  final int upcomingSeances;
  final double? riskScore;
  final String? riskCategory;
  final double? progressPercentage;

  PatientStatsModel({
    required this.totalSeances,
    required this.completedSeances,
    required this.upcomingSeances,
    this.riskScore,
    this.riskCategory,
    this.progressPercentage,
  });

  factory PatientStatsModel.fromPatientJson(Map<String, dynamic> json) {
    return PatientStatsModel(
      totalSeances: json['totalSeances'] as int? ?? 0,
      completedSeances: json['completedSeances'] as int? ?? 0,
      upcomingSeances: 0, // Will be calculated from seances list
      riskScore: json['riskScore'] != null ? (json['riskScore'] as num).toDouble() : null,
      riskCategory: json['riskCategory'] as String?,
      progressPercentage: null, // Will be calculated
    );
  }

  PatientStatsModel copyWith({
    int? totalSeances,
    int? completedSeances,
    int? upcomingSeances,
    double? riskScore,
    String? riskCategory,
    double? progressPercentage,
  }) {
    return PatientStatsModel(
      totalSeances: totalSeances ?? this.totalSeances,
      completedSeances: completedSeances ?? this.completedSeances,
      upcomingSeances: upcomingSeances ?? this.upcomingSeances,
      riskScore: riskScore ?? this.riskScore,
      riskCategory: riskCategory ?? this.riskCategory,
      progressPercentage: progressPercentage ?? this.progressPercentage,
    );
  }

  double get completionRate {
    if (totalSeances == 0) return 0;
    return (completedSeances / totalSeances) * 100;
  }
}
