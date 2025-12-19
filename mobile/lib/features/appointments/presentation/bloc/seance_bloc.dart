import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../data/repositories/seance_repository.dart';
import '../../data/models/seance_model.dart';

// Events
abstract class SeanceEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadPatientSeances extends SeanceEvent {
  final int patientId;
  final String token;
  
  LoadPatientSeances(this.patientId, this.token);
  
  @override
  List<Object?> get props => [patientId, token];
}

// States
abstract class SeanceState extends Equatable {
  @override
  List<Object?> get props => [];
}

class SeanceInitial extends SeanceState {}

class SeanceLoading extends SeanceState {}

class SeancesLoaded extends SeanceState {
  final List<SeanceModel> seances;
  
  SeancesLoaded(this.seances);
  
  @override
  List<Object?> get props => [seances];
}

class SeanceError extends SeanceState {
  final String message;
  
  SeanceError(this.message);
  
  @override
  List<Object?> get props => [message];
}

// Bloc
class SeanceBloc extends Bloc<SeanceEvent, SeanceState> {
  final SeanceRepository seanceRepository;

  SeanceBloc({required this.seanceRepository}) : super(SeanceInitial()) {
    on<LoadPatientSeances>(_onLoadPatientSeances);
  }

  Future<void> _onLoadPatientSeances(LoadPatientSeances event, Emitter<SeanceState> emit) async {
    emit(SeanceLoading());
    try {
      final seancesData = await seanceRepository.getPatientSeances(event.patientId, event.token);
      final seances = seancesData.map((s) => SeanceModel.fromJson(s)).toList();
      emit(SeancesLoaded(seances));
    } catch (e) {
      emit(SeanceError(e.toString().replaceAll('Exception: ', '')));
    }
  }
}
