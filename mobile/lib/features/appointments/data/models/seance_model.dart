class SeanceModel {
  final int id;
  final String seanceCode;
  final int patientId;
  final String? patientName;
  final int therapeuteId;
  final String? therapeuteName;
  final String type;
  final String status;
  final DateTime scheduledAt;
  final int durationMinutes;
  final String? notes;
  final bool isVideoSession;
  final DateTime createdAt;

  SeanceModel({
    required this.id,
    required this.seanceCode,
    required this.patientId,
    this.patientName,
    required this.therapeuteId,
    this.therapeuteName,
    required this.type,
    required this.status,
    required this.scheduledAt,
    required this.durationMinutes,
    this.notes,
    required this.isVideoSession,
    required this.createdAt,
  });

  factory SeanceModel.fromJson(Map<String, dynamic> json) {
    return SeanceModel(
      id: json['id'] as int,
      seanceCode: json['seanceCode'] as String,
      patientId: json['patientId'] as int,
      patientName: json['patientName'] as String?,
      therapeuteId: json['therapeuteId'] as int,
      therapeuteName: json['therapeuteName'] as String?,
      type: json['type'] as String,
      status: json['status'] as String,
      scheduledAt: DateTime.parse(json['scheduledAt'] as String),
      durationMinutes: json['durationMinutes'] as int,
      notes: json['notes'] as String?,
      isVideoSession: json['isVideoSession'] as bool? ?? false,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'seanceCode': seanceCode,
      'patientId': patientId,
      'patientName': patientName,
      'therapeuteId': therapeuteId,
      'therapeuteName': therapeuteName,
      'type': type,
      'status': status,
      'scheduledAt': scheduledAt.toIso8601String(),
      'durationMinutes': durationMinutes,
      'notes': notes,
      'isVideoSession': isVideoSession,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}
