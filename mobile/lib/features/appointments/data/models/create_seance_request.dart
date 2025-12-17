class CreateSeanceRequest {
  final int patientId;
  final int therapeuteId;
  final DateTime scheduledAt;
  final int durationMinutes;
  final String type;
  final String? initialStatus;
  final String? objectives;
  final String? notes;

  CreateSeanceRequest({
    required this.patientId,
    required this.therapeuteId,
    required this.scheduledAt,
    this.durationMinutes = 60,
    this.type = 'IN_PERSON',
    this.initialStatus,
    this.objectives,
    this.notes,
  });

  Map<String, dynamic> toJson() {
    return {
      'patientId': patientId,
      'therapeuteId': therapeuteId,
      'scheduledAt': scheduledAt.toIso8601String(),
      'durationMinutes': durationMinutes,
      'type': type,
      if (initialStatus != null) 'initialStatus': initialStatus,
      if (objectives != null) 'objectives': objectives,
      if (notes != null) 'notes': notes,
    };
  }
}
