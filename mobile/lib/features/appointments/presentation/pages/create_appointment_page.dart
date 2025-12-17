import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dio/dio.dart';

import '../../../auth/data/repositories/auth_repository.dart';
import '../../../auth/data/models/therapeute_model.dart';
import '../../data/models/create_seance_request.dart';
import '../../data/repositories/seance_repository.dart';
import '../bloc/seance_bloc.dart';
import '../bloc/seance_event.dart';
import '../bloc/seance_state.dart';

class CreateAppointmentPage extends StatefulWidget {
  const CreateAppointmentPage({super.key});

  @override
  State<CreateAppointmentPage> createState() => _CreateAppointmentPageState();
}

class _CreateAppointmentPageState extends State<CreateAppointmentPage> {
  final _formKey = GlobalKey<FormState>();
  int? _selectedTherapeuteId;
  DateTime? _selectedDate;
  TimeOfDay? _selectedTime;
  final _notesController = TextEditingController();
  bool _isCheckingConflict = false;
  bool _hasConflict = false;

  List<Map<String, dynamic>> _therapeutes = [];
  bool _isLoadingTherapeutes = true;

  @override
  void initState() {
    super.initState();
    _loadTherapeutes();
  }

  Future<void> _loadTherapeutes() async {
    try {
      final authRepo = AuthRepository();
      final therapeutesList = await authRepo.getAvailableTherapeutes();
      
      setState(() {
        _therapeutes = therapeutesList.map((t) => {
          'id': t.id,
          'firstName': t.firstName,
          'lastName': t.lastName,
          'specialization': t.specialty ?? 'Psychologue',
        }).toList();
        _isLoadingTherapeutes = false;
      });
    } catch (e) {
      setState(() {
        _isLoadingTherapeutes = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur de chargement des thérapeutes: $e')),
        );
      }
    }
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now().add(const Duration(days: 1)),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Future<void> _selectTime() async {
    final TimeOfDay? picked = await showTimePicker(
      context: context,
      initialTime: const TimeOfDay(hour: 9, minute: 0),
    );
    
    if (picked != null) {
      setState(() {
        _selectedTime = picked;
      });
      
      // Check for conflicts if all required fields are filled
      if (_selectedTherapeuteId != null && _selectedDate != null) {
        await _checkConflict();
      }
    }
  }

  Future<void> _checkConflict() async {
    if (_selectedTherapeuteId == null || _selectedDate == null || _selectedTime == null) {
      return;
    }

    setState(() {
      _isCheckingConflict = true;
      _hasConflict = false;
    });

    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('auth_token') ?? '';
      
      final scheduledAt = DateTime(
        _selectedDate!.year,
        _selectedDate!.month,
        _selectedDate!.day,
        _selectedTime!.hour,
        _selectedTime!.minute,
      );

      context.read<SeanceBloc>().add(CheckSeanceConflict(
        _selectedTherapeuteId!,
        scheduledAt,
        60,
        token,
      ));
    } catch (e) {
      setState(() {
        _isCheckingConflict = false;
      });
    }
  }

  Future<void> _submitForm() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_selectedTherapeuteId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Veuillez sélectionner un thérapeute')),
      );
      return;
    }

    if (_selectedDate == null || _selectedTime == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Veuillez sélectionner une date et une heure')),
      );
      return;
    }

    if (_hasConflict) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Ce créneau n\'est pas disponible')),
      );
      return;
    }

    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('auth_token') ?? '';
      final userId = prefs.getInt('user_id') ?? 0;

      // Get patient ID from user ID using the patient repository
      final dio = Dio(BaseOptions(
        baseUrl: 'http://localhost:8080/api',
        headers: {'Authorization': 'Bearer $token'},
      ));
      
      final patientResponse = await dio.get('/patients/user/$userId');
      final patientId = patientResponse.data['id'] as int;
      
      final scheduledAt = DateTime(
        _selectedDate!.year,
        _selectedDate!.month,
        _selectedDate!.day,
        _selectedTime!.hour,
        _selectedTime!.minute,
      );

      final request = CreateSeanceRequest(
        patientId: patientId,
        therapeuteId: _selectedTherapeuteId!,
        scheduledAt: scheduledAt,
        durationMinutes: 60,
        initialStatus: 'PENDING_APPROVAL',
        notes: _notesController.text.isEmpty ? null : _notesController.text,
      );

      context.read<SeanceBloc>().add(CreateSeance(request, token));
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      appBar: AppBar(
        title: const Text('Nouveau rendez-vous'),
        backgroundColor: const Color(0xFF00B4D8),
        foregroundColor: Colors.white,
      ),
      body: BlocListener<SeanceBloc, SeanceState>(
        listener: (context, state) {
          if (state is SeanceConflictChecked) {
            setState(() {
              _isCheckingConflict = false;
              _hasConflict = state.hasConflict;
            });
            
            if (state.hasConflict) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('⚠️ Ce créneau n\'est pas disponible'),
                  backgroundColor: Colors.orange,
                ),
              );
            } else {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('✓ Créneau disponible'),
                  backgroundColor: Colors.green,
                ),
              );
            }
          } else if (state is SeanceCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Demande de rendez-vous envoyée avec succès!')),
            );
            Navigator.pop(context, true); // Return true to indicate success
          } else if (state is SeanceError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Erreur: ${state.message}')),
            );
          }
        },
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Sélectionnez un thérapeute',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 12),
                        _isLoadingTherapeutes
                            ? const Center(child: CircularProgressIndicator())
                            : DropdownButtonFormField<int>(
                                value: _selectedTherapeuteId,
                                decoration: const InputDecoration(
                                  border: OutlineInputBorder(),
                                  hintText: 'Choisir un thérapeute',
                                ),
                                items: _therapeutes.map((t) {
                                  return DropdownMenuItem<int>(
                                    value: t['id'] as int,
                                    child: Text('${t['firstName']} ${t['lastName']} - ${t['specialization']}'),
                                  );
                                }).toList(),
                                onChanged: (value) {
                                  setState(() {
                                    _selectedTherapeuteId = value;
                                  });
                                  if (_selectedDate != null && _selectedTime != null) {
                                    _checkConflict();
                                  }
                                },
                                validator: (value) {
                                  if (value == null) {
                                    return 'Veuillez sélectionner un thérapeute';
                                  }
                                  return null;
                                },
                              ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Date et heure',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 12),
                        ListTile(
                          leading: const Icon(Icons.calendar_today, color: Color(0xFF00B4D8)),
                          title: Text(
                            _selectedDate == null
                                ? 'Sélectionner une date'
                                : DateFormat('dd/MM/yyyy').format(_selectedDate!),
                          ),
                          trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                          onTap: _selectDate,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                            side: const BorderSide(color: Colors.grey),
                          ),
                        ),
                        const SizedBox(height: 12),
                        ListTile(
                          leading: const Icon(Icons.access_time, color: Color(0xFF00B4D8)),
                          title: Text(
                            _selectedTime == null
                                ? 'Sélectionner une heure'
                                : _selectedTime!.format(context),
                          ),
                          trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                          onTap: _selectTime,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                            side: const BorderSide(color: Colors.grey),
                          ),
                        ),
                        if (_isCheckingConflict)
                          const Padding(
                            padding: EdgeInsets.all(8.0),
                            child: Center(
                              child: CircularProgressIndicator(),
                            ),
                          ),
                        if (_hasConflict && !_isCheckingConflict)
                          Container(
                            margin: const EdgeInsets.only(top: 12),
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: Colors.orange.shade50,
                              borderRadius: BorderRadius.circular(8),
                              border: Border.all(color: Colors.orange),
                            ),
                            child: const Row(
                              children: [
                                Icon(Icons.warning, color: Colors.orange),
                                SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    'Ce créneau n\'est pas disponible. Veuillez choisir une autre heure.',
                                    style: TextStyle(color: Colors.orange),
                                  ),
                                ),
                              ],
                            ),
                          ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Notes (optionnel)',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 12),
                        TextFormField(
                          controller: _notesController,
                          maxLines: 4,
                          decoration: const InputDecoration(
                            border: OutlineInputBorder(),
                            hintText: 'Ajoutez des notes pour le thérapeute...',
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                BlocBuilder<SeanceBloc, SeanceState>(
                  builder: (context, state) {
                    final isCreating = state is SeanceCreating;
                    return SizedBox(
                      width: double.infinity,
                      height: 50,
                      child: ElevatedButton(
                        onPressed: (isCreating || _hasConflict) ? null : _submitForm,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF00B4D8),
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        child: isCreating
                            ? const CircularProgressIndicator(color: Colors.white)
                            : const Text(
                                'Envoyer la demande',
                                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                              ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 16),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.blue.shade50,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Row(
                    children: [
                      Icon(Icons.info_outline, color: Colors.blue),
                      SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'Votre demande sera envoyée au thérapeute pour approbation. Vous recevrez une notification une fois qu\'elle sera confirmée.',
                          style: TextStyle(color: Colors.blue, fontSize: 12),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }
}
