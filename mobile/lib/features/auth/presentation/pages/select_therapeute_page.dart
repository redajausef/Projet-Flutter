import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_theme.dart';

class SelectTherapeutePage extends StatelessWidget {
  final Map<String, dynamic> patientData;

  const SelectTherapeutePage({super.key, required this.patientData});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Choisir un thérapeute'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.person_search, size: 80, color: AppColors.primary),
            const SizedBox(height: 24),
            const Text(
              'Fonctionnalité en développement',
              style: TextStyle(fontSize: 18),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: () => context.go('/login'),
              child: const Text('Retour à la connexion'),
            ),
          ],
        ),
      ),
    );
  }
}
