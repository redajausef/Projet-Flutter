import 'package:equatable/equatable.dart';

import '../../data/models/prediction_model.dart';

abstract class PredictionState extends Equatable {
  const PredictionState();

  @override
  List<Object?> get props => [];
}

class PredictionInitial extends PredictionState {}

class PredictionLoading extends PredictionState {}

class PredictionsLoaded extends PredictionState {
  final List<PredictionModel> predictions;

  const PredictionsLoaded(this.predictions);

  @override
  List<Object?> get props => [predictions];
}

class PredictionGenerated extends PredictionState {
  final PredictionModel prediction;

  const PredictionGenerated(this.prediction);

  @override
  List<Object?> get props => [prediction];
}

class PredictionError extends PredictionState {
  final String message;

  const PredictionError(this.message);

  @override
  List<Object?> get props => [message];
}
