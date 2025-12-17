import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/repositories/patient_repository.dart';
import 'home_event.dart';
import 'home_state.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  final PatientRepository patientRepository;

  HomeBloc({required this.patientRepository}) : super(HomeInitial()) {
    on<LoadPatientStats>(_onLoadPatientStats);
  }

  Future<void> _onLoadPatientStats(
    LoadPatientStats event,
    Emitter<HomeState> emit,
  ) async {
    emit(HomeLoading());
    try {
      final stats = await patientRepository.getPatientStats(event.patientId, event.token);
      emit(HomeLoaded(stats));
    } catch (e) {
      emit(HomeError(e.toString()));
    }
  }
}
