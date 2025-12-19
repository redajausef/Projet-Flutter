import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../data/repositories/auth_repository.dart';

// Events
abstract class RegisterEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class RegisterSubmitted extends RegisterEvent {
  final Map<String, dynamic> userData;
  
  RegisterSubmitted(this.userData);
  
  @override
  List<Object?> get props => [userData];
}

// States
abstract class RegisterState extends Equatable {
  @override
  List<Object?> get props => [];
}

class RegisterInitial extends RegisterState {}

class RegisterLoading extends RegisterState {}

class RegisterSuccess extends RegisterState {
  final Map<String, dynamic> user;
  
  RegisterSuccess(this.user);
  
  @override
  List<Object?> get props => [user];
}

class RegisterError extends RegisterState {
  final String message;
  
  RegisterError(this.message);
  
  @override
  List<Object?> get props => [message];
}

// Bloc
class RegisterBloc extends Bloc<RegisterEvent, RegisterState> {
  final AuthRepository authRepository;

  RegisterBloc({required this.authRepository}) : super(RegisterInitial()) {
    on<RegisterSubmitted>(_onRegisterSubmitted);
  }

  Future<void> _onRegisterSubmitted(RegisterSubmitted event, Emitter<RegisterState> emit) async {
    emit(RegisterLoading());
    try {
      final result = await authRepository.register(event.userData);
      emit(RegisterSuccess(result));
    } catch (e) {
      emit(RegisterError(e.toString().replaceAll('Exception: ', '')));
    }
  }
}
