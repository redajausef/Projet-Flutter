import 'package:equatable/equatable.dart';

import '../../data/models/therapeute_model.dart';

abstract class RegisterState extends Equatable {
  const RegisterState();

  @override
  List<Object?> get props => [];
}

class RegisterInitial extends RegisterState {}

class RegisterLoading extends RegisterState {}

class TherapeutesLoading extends RegisterState {}

class TherapeutesLoaded extends RegisterState {
  final List<TherapeuteModel> therapeutes;

  const TherapeutesLoaded(this.therapeutes);

  @override
  List<Object?> get props => [therapeutes];
}

class TherapeutesError extends RegisterState {
  final String message;

  const TherapeutesError(this.message);

  @override
  List<Object?> get props => [message];
}

class RegisterSuccess extends RegisterState {
  final Map<String, dynamic> patient;

  const RegisterSuccess(this.patient);

  @override
  List<Object?> get props => [patient];
}

class RegisterFailure extends RegisterState {
  final String message;

  const RegisterFailure(this.message);

  @override
  List<Object?> get props => [message];
}
