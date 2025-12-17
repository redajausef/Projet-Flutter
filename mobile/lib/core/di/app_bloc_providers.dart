import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../features/appointments/data/repositories/seance_repository.dart';
import '../../features/appointments/presentation/bloc/seance_bloc.dart';
import '../../features/auth/data/repositories/auth_repository.dart';
import '../../features/auth/presentation/bloc/auth_bloc.dart';
import '../../features/home/data/repositories/patient_repository.dart';
import '../../features/home/presentation/bloc/home_bloc.dart';
import '../../features/predictions/data/repositories/prediction_repository.dart';
import '../../features/predictions/presentation/bloc/prediction_bloc.dart';
import '../../features/auth/presentation/bloc/register_bloc.dart';

class AppBlocProviders extends StatelessWidget {
  final Widget child;

  const AppBlocProviders({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<AuthBloc>(
          create: (context) => AuthBloc(authRepository: AuthRepository())
            ..add(CheckAuthStatus()), // Check auth status on startup
        ),
        BlocProvider<RegisterBloc>(
          create: (context) => RegisterBloc(authRepository: AuthRepository()),
        ),
        BlocProvider<HomeBloc>(
          create: (context) => HomeBloc(patientRepository: PatientRepository()),
        ),
        BlocProvider<SeanceBloc>(
          create: (context) => SeanceBloc(seanceRepository: SeanceRepository()),
        ),
        BlocProvider<PredictionBloc>(
          create: (context) => PredictionBloc(predictionRepository: PredictionRepository()),
        ),
      ],
      child: child,
    );
  }
}
