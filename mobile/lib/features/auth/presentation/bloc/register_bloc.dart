import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/repositories/auth_repository.dart';
import 'register_event.dart';
import 'register_state.dart';

class RegisterBloc extends Bloc<RegisterEvent, RegisterState> {
  final AuthRepository authRepository;

  RegisterBloc({required this.authRepository}) : super(RegisterInitial()) {
    on<LoadTherapeutes>(_onLoadTherapeutes);
    on<RegisterPatient>(_onRegisterPatient);
  }

  Future<void> _onLoadTherapeutes(
    LoadTherapeutes event,
    Emitter<RegisterState> emit,
  ) async {
    emit(TherapeutesLoading());
    try {
      final therapeutes = await authRepository.getAvailableTherapeutes();
      emit(TherapeutesLoaded(therapeutes));
    } catch (e) {
      emit(TherapeutesError(e.toString()));
    }
  }

  Future<void> _onRegisterPatient(
    RegisterPatient event,
    Emitter<RegisterState> emit,
  ) async {
    emit(RegisterLoading());
    try {
      final patient = await authRepository.registerPatient(
        email: event.email,
        firstName: event.firstName,
        lastName: event.lastName,
        phoneNumber: event.phoneNumber,
        dateOfBirth: event.dateOfBirth,
        gender: event.gender,
        password: event.password,
        address: event.address,
        assignedTherapeuteId: event.assignedTherapeuteId,
      );
      emit(RegisterSuccess(patient));
    } catch (e) {
      emit(RegisterFailure(e.toString()));
    }
  }
}
