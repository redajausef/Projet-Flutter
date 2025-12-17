import 'package:equatable/equatable.dart';

abstract class HomeEvent extends Equatable {
  const HomeEvent();

  @override
  List<Object?> get props => [];
}

class LoadPatientStats extends HomeEvent {
  final int patientId;
  final String? token;

  const LoadPatientStats(this.patientId, this.token);

  @override
  List<Object?> get props => [patientId, token];
}
