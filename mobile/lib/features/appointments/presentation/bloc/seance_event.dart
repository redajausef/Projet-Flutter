import 'package:equatable/equatable.dart';

import '../../data/models/seance_model.dart';
import '../../data/models/create_seance_request.dart';

abstract class SeanceEvent extends Equatable {
  const SeanceEvent();

  @override
  List<Object?> get props => [];
}

class LoadPatientSeances extends SeanceEvent {
  final int patientId;
  final String? token;

  const LoadPatientSeances(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}

class LoadUpcomingSeances extends SeanceEvent {
  final int patientId;
  final String? token;

  const LoadUpcomingSeances(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}

class CreateSeance extends SeanceEvent {
  final CreateSeanceRequest request;
  final String token;

  const CreateSeance(this.request, this.token);

  @override
  List<Object?> get props => [request, token];
}

class CheckSeanceConflict extends SeanceEvent {
  final int therapeuteId;
  final DateTime scheduledAt;
  final int durationMinutes;
  final String token;

  const CheckSeanceConflict(
    this.therapeuteId,
    this.scheduledAt,
    this.durationMinutes,
    this.token,
  );

  @override
  List<Object?> get props => [therapeuteId, scheduledAt, durationMinutes, token];
}
