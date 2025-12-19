import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import '../../../../core/theme/app_theme.dart';

class SelectTherapeutePage extends StatefulWidget {
  final Map<String, dynamic> patientData;

  const SelectTherapeutePage({super.key, required this.patientData});

  @override
  State<SelectTherapeutePage> createState() => _SelectTherapeutePageState();
}

class _SelectTherapeutePageState extends State<SelectTherapeutePage> {
  List<Map<String, dynamic>> _therapeutes = [];
  bool _isLoading = true;
  String? _error;
  int? _selectedTherapeuteId;
  bool _isRegistering = false;

  @override
  void initState() {
    super.initState();
    _loadTherapeutes();
  }

  Future<void> _loadTherapeutes() async {
    try {
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080/api'));
      final response = await dio.get('/therapeutes');
      
      final content = response.data['content'] as List? ?? response.data as List? ?? [];
      
      setState(() {
        _therapeutes = content.map((t) => t as Map<String, dynamic>).toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Impossible de charger les thérapeutes';
        _isLoading = false;
      });
    }
  }

  Future<void> _completeRegistration() async {
    if (_selectedTherapeuteId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Veuillez sélectionner un thérapeute'), backgroundColor: AppColors.warning),
      );
      return;
    }

    setState(() => _isRegistering = true);

    try {
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080/api'));
      
      // Map Flutter fields to backend expected fields
      final registerData = {
        'username': widget.patientData['email']?.split('@').first ?? 'user${DateTime.now().millisecondsSinceEpoch}',
        'email': widget.patientData['email'],
        'password': widget.patientData['password'],
        'firstName': widget.patientData['prenom'],
        'lastName': widget.patientData['nom'],
        'phoneNumber': widget.patientData['phone'],
        'role': 'PATIENT',
      };
      
      await dio.post('/auth/register', data: registerData);
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Inscription réussie ! Connectez-vous.'),
            backgroundColor: AppColors.success,
          ),
        );
        context.go('/login');
      }
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Erreur lors de l\'inscription';
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message), backgroundColor: AppColors.error),
      );
    } finally {
      if (mounted) setState(() => _isRegistering = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: AppColors.backgroundGradient),
        child: SafeArea(
          child: Column(
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(24),
                child: Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.arrow_back),
                      onPressed: () => context.pop(),
                    ),
                    const SizedBox(width: 8),
                    const Expanded(
                      child: Text(
                        'Choisir un thérapeute',
                        style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
              ),
              
              // Subtitle
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Text(
                  'Sélectionnez le thérapeute qui vous accompagnera',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 14),
                ),
              ),
              const SizedBox(height: 20),
              
              // Content
              Expanded(
                child: _isLoading
                  ? const Center(child: CircularProgressIndicator())
                  : _error != null
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.error_outline, size: 48, color: AppColors.error),
                            const SizedBox(height: 16),
                            Text(_error!),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: _loadTherapeutes,
                              child: const Text('Réessayer'),
                            ),
                          ],
                        ),
                      )
                    : ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 24),
                        itemCount: _therapeutes.length,
                        itemBuilder: (context, index) {
                          final therapeute = _therapeutes[index];
                          return _TherapeuteCard(
                            therapeute: therapeute,
                            isSelected: _selectedTherapeuteId == therapeute['id'],
                            onTap: () => setState(() => _selectedTherapeuteId = therapeute['id']),
                          );
                        },
                      ),
              ),
              
              // Bottom button
              Padding(
                padding: const EdgeInsets.all(24),
                child: SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: ElevatedButton(
                    onPressed: _isRegistering ? null : _completeRegistration,
                    child: _isRegistering
                      ? const SizedBox(
                          width: 24, height: 24,
                          child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                        )
                      : const Text('Confirmer et s\'inscrire'),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TherapeuteCard extends StatelessWidget {
  final Map<String, dynamic> therapeute;
  final bool isSelected;
  final VoidCallback onTap;

  const _TherapeuteCard({
    required this.therapeute,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final name = therapeute['fullName'] ?? therapeute['userName'] ?? 'Thérapeute';
    final specialty = therapeute['specialization'] ?? therapeute['specialty'] ?? 'Thérapeute';
    final patientsCount = therapeute['totalPatients'] ?? 0;

    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary.withOpacity(0.1) : AppColors.card,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected ? AppColors.primary : Colors.transparent,
            width: 2,
          ),
        ),
        child: Row(
          children: [
            // Avatar
            CircleAvatar(
              radius: 28,
              backgroundColor: AppColors.primary.withOpacity(0.1),
              child: Text(
                name.isNotEmpty ? name[0].toUpperCase() : 'T',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
            ),
            const SizedBox(width: 16),
            
            // Info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    specialty,
                    style: TextStyle(fontSize: 13, color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Icon(Icons.people, size: 14, color: AppColors.textMuted),
                      const SizedBox(width: 4),
                      Text(
                        '$patientsCount patients',
                        style: TextStyle(fontSize: 12, color: AppColors.textMuted),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            
            // Selection indicator
            Container(
              width: 24,
              height: 24,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: isSelected ? AppColors.primary : Colors.transparent,
                border: Border.all(
                  color: isSelected ? AppColors.primary : AppColors.border,
                  width: 2,
                ),
              ),
              child: isSelected
                ? const Icon(Icons.check, size: 16, color: Colors.white)
                : null,
            ),
          ],
        ),
      ),
    );
  }
}
