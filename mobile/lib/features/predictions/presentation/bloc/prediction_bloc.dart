import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../data/repositories/prediction_repository.dart';

// Events
abstract class PredictionEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadPatientPredictions extends PredictionEvent {
  final int patientId;
  final String token;
  
  LoadPatientPredictions(this.patientId, this.token);
  
  @override
  List<Object?> get props => [patientId, token];
}

class GenerateDropoutRiskPrediction extends PredictionEvent {
  final int patientId;
  final String token;
  
  GenerateDropoutRiskPrediction(this.patientId, this.token);
  
  @override
  List<Object?> get props => [patientId, token];
}

// States
abstract class PredictionState extends Equatable {
  @override
  List<Object?> get props => [];
}

class PredictionInitial extends PredictionState {}

class PredictionLoading extends PredictionState {}

class PredictionsLoaded extends PredictionState {
  final List<Map<String, dynamic>> predictions;
  
  PredictionsLoaded(this.predictions);
  
  @override
  List<Object?> get props => [predictions];
}

class PredictionError extends PredictionState {
  final String message;
  
  PredictionError(this.message);
  
  @override
  List<Object?> get props => [message];
}

// Bloc
class PredictionBloc extends Bloc<PredictionEvent, PredictionState> {
  final PredictionRepository predictionRepository;

  PredictionBloc({required this.predictionRepository}) : super(PredictionInitial()) {
    on<LoadPatientPredictions>(_onLoadPatientPredictions);
    on<GenerateDropoutRiskPrediction>(_onGeneratePrediction);
  }

  Future<void> _onLoadPatientPredictions(LoadPatientPredictions event, Emitter<PredictionState> emit) async {
    emit(PredictionLoading());
    try {
      final predictions = await predictionRepository.getPatientPredictions(event.patientId, event.token);
      emit(PredictionsLoaded(predictions));
    } catch (e) {
      emit(PredictionError(e.toString().replaceAll('Exception: ', '')));
    }
  }

  Future<void> _onGeneratePrediction(GenerateDropoutRiskPrediction event, Emitter<PredictionState> emit) async {
    emit(PredictionLoading());
    try {
      await predictionRepository.generatePrediction(event.patientId, event.token);
      // Reload predictions after generating
      final predictions = await predictionRepository.getPatientPredictions(event.patientId, event.token);
      emit(PredictionsLoaded(predictions));
    } catch (e) {
      emit(PredictionError(e.toString().replaceAll('Exception: ', '')));
    }
  }
}
