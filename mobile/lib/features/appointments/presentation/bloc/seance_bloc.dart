import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/repositories/seance_repository.dart';
import 'seance_event.dart';
import 'seance_state.dart';

class SeanceBloc extends Bloc<SeanceEvent, SeanceState> {
  final SeanceRepository seanceRepository;

  SeanceBloc({required this.seanceRepository}) : super(SeanceInitial()) {
    on<LoadPatientSeances>(_onLoadPatientSeances);
    on<LoadUpcomingSeances>(_onLoadUpcomingSeances);
    on<CreateSeance>(_onCreateSeance);
    on<CheckSeanceConflict>(_onCheckSeanceConflict);
  }

  Future<void> _onLoadPatientSeances(
    LoadPatientSeances event,
    Emitter<SeanceState> emit,
  ) async {
    emit(SeanceLoading());
    try {
      final seances = await seanceRepository.getPatientSeances(event.patientId, event.token);
      emit(SeancesLoaded(seances));
    } catch (e) {
      emit(SeanceError(e.toString()));
    }
  }

  Future<void> _onLoadUpcomingSeances(
    LoadUpcomingSeances event,
    Emitter<SeanceState> emit,
  ) async {
    emit(SeanceLoading());
    try {
      final seances = await seanceRepository.getUpcomingSeances(event.patientId, event.token);
      emit(SeancesLoaded(seances));
    } catch (e) {
      emit(SeanceError(e.toString()));
    }
  }

  Future<void> _onCreateSeance(
    CreateSeance event,
    Emitter<SeanceState> emit,
  ) async {
    emit(SeanceCreating());
    try {
      final seance = await seanceRepository.createSeance(event.request, event.token);
      emit(SeanceCreated(seance));
    } catch (e) {
      emit(SeanceError(e.toString()));
    }
  }

  Future<void> _onCheckSeanceConflict(
    CheckSeanceConflict event,
    Emitter<SeanceState> emit,
  ) async {
    emit(SeanceConflictChecking());
    try {
      final hasConflict = await seanceRepository.checkConflict(
        event.therapeuteId,
        event.scheduledAt,
        event.durationMinutes,
        event.token,
      );
      emit(SeanceConflictChecked(hasConflict));
    } catch (e) {
      emit(SeanceError(e.toString()));
    }
  }
}
