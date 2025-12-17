import 'package:equatable/equatable.dart';

abstract class PredictionEvent extends Equatable {
  const PredictionEvent();

  @override
  List<Object?> get props => [];
}

class LoadPatientPredictions extends PredictionEvent {
  final int patientId;
  final String? token;

  const LoadPatientPredictions(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}

class GenerateNextSessionPrediction extends PredictionEvent {
  final int patientId;
  final String? token;

  const GenerateNextSessionPrediction(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}

class GenerateDropoutRiskPrediction extends PredictionEvent {
  final int patientId;
  final String? token;

  const GenerateDropoutRiskPrediction(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}
