class SeanceModel {
  final int id;
  final int patientId;
  final int therapeuteId;
  final String? therapeuteName;
  final String type;
  final String status;
  final DateTime scheduledAt;
  final int durationMinutes;
  final String? notes;

  SeanceModel({
    required this.id,
    required this.patientId,
    required this.therapeuteId,
    this.therapeuteName,
    required this.type,
    required this.status,
    required this.scheduledAt,
    this.durationMinutes = 60,
    this.notes,
  });

  factory SeanceModel.fromJson(Map<String, dynamic> json) {
    return SeanceModel(
      id: json['id'] ?? 0,
      patientId: json['patientId'] ?? 0,
      therapeuteId: json['therapeuteId'] ?? 0,
      therapeuteName: json['therapeuteName'],
      type: json['type'] ?? 'IN_PERSON',
      status: json['status'] ?? 'SCHEDULED',
      scheduledAt: DateTime.tryParse(json['scheduledAt'] ?? '') ?? DateTime.now(),
      durationMinutes: json['durationMinutes'] ?? 60,
      notes: json['notes'],
    );
  }

  bool get isVideoSession => type == 'VIDEO_CALL';
  bool get isUpcoming => scheduledAt.isAfter(DateTime.now());
}
