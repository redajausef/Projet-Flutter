import 'package:equatable/equatable.dart';

import '../../data/models/therapeute_model.dart';

abstract class RegisterEvent extends Equatable {
  const RegisterEvent();

  @override
  List<Object?> get props => [];
}

class LoadTherapeutes extends RegisterEvent {}

class RegisterPatient extends RegisterEvent {
  final String email;
  final String firstName;
  final String lastName;
  final String phoneNumber;
  final DateTime dateOfBirth;
  final String gender;
  final String password;
  final String? address;
  final int? assignedTherapeuteId;

  const RegisterPatient({
    required this.email,
    required this.firstName,
    required this.lastName,
    required this.phoneNumber,
    required this.dateOfBirth,
    required this.gender,
    required this.password,
    this.address,
    this.assignedTherapeuteId,
  });

  @override
  List<Object?> get props => [
        email,
        firstName,
        lastName,
        phoneNumber,
        dateOfBirth,
        gender,
        password,
        address,
        assignedTherapeuteId,
      ];
}
