import 'package:equatable/equatable.dart';

import '../../data/models/patient_stats_model.dart';

abstract class HomeState extends Equatable {
  const HomeState();

  @override
  List<Object?> get props => [];
}

class HomeInitial extends HomeState {}

class HomeLoading extends HomeState {}

class HomeLoaded extends HomeState {
  final PatientStatsModel stats;

  const HomeLoaded(this.stats);

  @override
  List<Object?> get props => [stats];
}

class HomeError extends HomeState {
  final String message;

  const HomeError(this.message);

  @override
  List<Object?> get props => [message];
}
