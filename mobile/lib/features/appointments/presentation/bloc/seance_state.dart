import 'package:equatable/equatable.dart';

import '../../data/models/seance_model.dart';

abstract class SeanceState extends Equatable {
  const SeanceState();

  @override
  List<Object?> get props => [];
}

class SeanceInitial extends SeanceState {}

class SeanceLoading extends SeanceState {}

class SeancesLoaded extends SeanceState {
  final List<SeanceModel> seances;

  const SeancesLoaded(this.seances);

  @override
  List<Object?> get props => [seances];
}

class SeanceError extends SeanceState {
  final String message;

  const SeanceError(this.message);

  @override
  List<Object?> get props => [message];
}

class SeanceCreating extends SeanceState {}

class SeanceCreated extends SeanceState {
  final SeanceModel seance;

  const SeanceCreated(this.seance);

  @override
  List<Object?> get props => [seance];
}

class SeanceConflictChecking extends SeanceState {}

class SeanceConflictChecked extends SeanceState {
  final bool hasConflict;

  const SeanceConflictChecked(this.hasConflict);

  @override
  List<Object?> get props => [hasConflict];
}
