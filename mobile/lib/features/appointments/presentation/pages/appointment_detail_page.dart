import 'package:flutter/material.dart';
import '../../../../core/theme/app_theme.dart';

class AppointmentDetailPage extends StatelessWidget {
  final String appointmentId;

  const AppointmentDetailPage({super.key, required this.appointmentId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Détails du rendez-vous'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.calendar_month, size: 64, color: AppColors.primary),
            const SizedBox(height: 16),
            Text('Rendez-vous #$appointmentId'),
          ],
        ),
      ),
    );
  }
}
