import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/repositories/prediction_repository.dart';
import 'prediction_event.dart';
import 'prediction_state.dart';

class PredictionBloc extends Bloc<PredictionEvent, PredictionState> {
  final PredictionRepository predictionRepository;

  PredictionBloc({required this.predictionRepository}) : super(PredictionInitial()) {
    on<LoadPatientPredictions>(_onLoadPatientPredictions);
    on<GenerateNextSessionPrediction>(_onGenerateNextSessionPrediction);
    on<GenerateDropoutRiskPrediction>(_onGenerateDropoutRiskPrediction);
  }

  Future<void> _onLoadPatientPredictions(
    LoadPatientPredictions event,
    Emitter<PredictionState> emit,
  ) async {
    emit(PredictionLoading());
    try {
      final predictions = await predictionRepository.getPatientPredictions(event.patientId, event.token);
      emit(PredictionsLoaded(predictions));
    } catch (e) {
      emit(PredictionError(e.toString()));
    }
  }

  Future<void> _onGenerateNextSessionPrediction(
    GenerateNextSessionPrediction event,
    Emitter<PredictionState> emit,
  ) async {
    emit(PredictionLoading());
    try {
      final prediction = await predictionRepository.generateNextSessionPrediction(event.patientId, event.token);
      emit(PredictionGenerated(prediction));
    } catch (e) {
      emit(PredictionError(e.toString()));
    }
  }

  Future<void> _onGenerateDropoutRiskPrediction(
    GenerateDropoutRiskPrediction event,
    Emitter<PredictionState> emit,
  ) async {
    emit(PredictionLoading());
    try {
      final prediction = await predictionRepository.generateDropoutRiskPrediction(event.patientId, event.token);
      emit(PredictionGenerated(prediction));
    } catch (e) {
      emit(PredictionError(e.toString()));
    }
  }
}
