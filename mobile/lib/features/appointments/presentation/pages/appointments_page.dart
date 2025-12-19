import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dio/dio.dart';
import 'package:table_calendar/table_calendar.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../bloc/seance_bloc.dart';
import '../../data/models/seance_model.dart';

class AppointmentsPage extends StatefulWidget {
  const AppointmentsPage({super.key});

  @override
  State<AppointmentsPage> createState() => _AppointmentsPageState();
}

class _AppointmentsPageState extends State<AppointmentsPage> {
  CalendarFormat _calendarFormat = CalendarFormat.week;
  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;
  int? _patientId;
  int? _therapeuteId;
  String? _token;

  @override
  void initState() {
    super.initState();
    _loadSeances();
  }

  void _loadSeances() async {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated) {
      try {
        final prefs = await SharedPreferences.getInstance();
        final userId = prefs.getInt('user_id');
        if (userId == null) return;
        
        final dio = Dio(BaseOptions(
          baseUrl: 'http://localhost:8080/api',
          headers: {'Authorization': 'Bearer ${authState.token}'},
        ));
        
        final patientResponse = await dio.get('/patients/user/$userId');
        final patientId = patientResponse.data['id'] as int;
        
        // Get therapeuteId from existing seances
        int? therapeuteId;
        try {
          final seancesResponse = await dio.get('/seances/patient/$patientId');
          final seances = seancesResponse.data as List;
          if (seances.isNotEmpty) {
            therapeuteId = seances.first['therapeuteId'] as int?;
          }
        } catch (e) {
          print('Could not fetch seances: $e');
        }
        
        // Fallback to first therapeute if no seances
        therapeuteId ??= 1;
        
        setState(() {
          _patientId = patientId;
          _therapeuteId = therapeuteId;
          _token = authState.token;
        });
        
        context.read<SeanceBloc>().add(LoadPatientSeances(patientId, authState.token));
      } catch (e) {
        print('Error loading seances: $e');
      }
    }
  }

  void _showRequestSeanceDialog() {
    DateTime selectedDate = DateTime.now().add(const Duration(days: 1));
    TimeOfDay selectedTime = const TimeOfDay(hour: 10, minute: 0);
    String selectedType = 'IN_PERSON';
    bool isLoading = false;
    String? errorMessage;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => StatefulBuilder(
        builder: (context, setModalState) {
          return Container(
            padding: EdgeInsets.only(
              bottom: MediaQuery.of(context).viewInsets.bottom,
            ),
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Demander une séance',
                        style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(context),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  
                  // Date picker
                  Text('Date', style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
                  const SizedBox(height: 8),
                  InkWell(
                    onTap: () async {
                      final date = await showDatePicker(
                        context: context,
                        initialDate: selectedDate,
                        firstDate: DateTime.now(),
                        lastDate: DateTime.now().add(const Duration(days: 90)),
                      );
                      if (date != null) {
                        setModalState(() => selectedDate = date);
                      }
                    },
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceLight,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.calendar_today, color: AppColors.primary),
                          const SizedBox(width: 12),
                          Text(DateFormat('EEEE d MMMM yyyy', 'fr_FR').format(selectedDate)),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  // Time picker
                  Text('Heure', style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
                  const SizedBox(height: 8),
                  InkWell(
                    onTap: () async {
                      final time = await showTimePicker(
                        context: context,
                        initialTime: selectedTime,
                      );
                      if (time != null) {
                        setModalState(() => selectedTime = time);
                      }
                    },
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceLight,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.access_time, color: AppColors.primary),
                          const SizedBox(width: 12),
                          Text('${selectedTime.hour.toString().padLeft(2, '0')}:${selectedTime.minute.toString().padLeft(2, '0')}'),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  // Type selector
                  Text('Type de séance', style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: InkWell(
                          onTap: () => setModalState(() => selectedType = 'IN_PERSON'),
                          child: Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: selectedType == 'IN_PERSON' 
                                ? AppColors.primary.withOpacity(0.1) 
                                : AppColors.surfaceLight,
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: selectedType == 'IN_PERSON' 
                                  ? AppColors.primary 
                                  : Colors.transparent,
                              ),
                            ),
                            child: Column(
                              children: [
                                Icon(Icons.location_on, 
                                  color: selectedType == 'IN_PERSON' ? AppColors.primary : AppColors.textMuted),
                                const SizedBox(height: 4),
                                Text('Présentiel', style: TextStyle(
                                  color: selectedType == 'IN_PERSON' ? AppColors.primary : AppColors.textSecondary,
                                  fontSize: 12,
                                )),
                              ],
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: InkWell(
                          onTap: () => setModalState(() => selectedType = 'VIDEO_CALL'),
                          child: Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: selectedType == 'VIDEO_CALL' 
                                ? AppColors.accent.withOpacity(0.1) 
                                : AppColors.surfaceLight,
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: selectedType == 'VIDEO_CALL' 
                                  ? AppColors.accent 
                                  : Colors.transparent,
                              ),
                            ),
                            child: Column(
                              children: [
                                Icon(Icons.videocam, 
                                  color: selectedType == 'VIDEO_CALL' ? AppColors.accent : AppColors.textMuted),
                                const SizedBox(height: 4),
                                Text('Vidéo', style: TextStyle(
                                  color: selectedType == 'VIDEO_CALL' ? AppColors.accent : AppColors.textSecondary,
                                  fontSize: 12,
                                )),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  
                  if (errorMessage != null) ...[
                    const SizedBox(height: 16),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.error.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.error_outline, color: AppColors.error, size: 18),
                          const SizedBox(width: 8),
                          Expanded(child: Text(errorMessage!, style: TextStyle(color: AppColors.error, fontSize: 13))),
                        ],
                      ),
                    ),
                  ],
                  
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton(
                      onPressed: isLoading ? null : () async {
                        setModalState(() {
                          isLoading = true;
                          errorMessage = null;
                        });
                        
                        final scheduledAt = DateTime(
                          selectedDate.year,
                          selectedDate.month,
                          selectedDate.day,
                          selectedTime.hour,
                          selectedTime.minute,
                        );
                        
                        final result = await _checkSlotAndSubmit(scheduledAt, selectedType);
                        
                        if (result == null) {
                          Navigator.pop(context);
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Demande envoyée ! En attente d\'approbation du thérapeute.'),
                              backgroundColor: AppColors.success,
                            ),
                          );
                          _loadSeances();
                        } else {
                          setModalState(() {
                            isLoading = false;
                            errorMessage = result;
                          });
                        }
                      },
                      child: isLoading 
                        ? const SizedBox(
                            width: 24, height: 24,
                            child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                          )
                        : const Text('Envoyer la demande'),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Future<String?> _checkSlotAndSubmit(DateTime scheduledAt, String type) async {
    if (_patientId == null || _therapeuteId == null || _token == null) {
      return 'Erreur: informations patient manquantes';
    }
    
    try {
      final dio = Dio(BaseOptions(
        baseUrl: 'http://localhost:8080/api',
        headers: {'Authorization': 'Bearer $_token'},
      ));
      
      // Submit seance request with PENDING_APPROVAL status (awaiting therapeute approval)
      await dio.post('/seances', data: {
        'patientId': _patientId,
        'therapeuteId': _therapeuteId,
        'scheduledAt': scheduledAt.toIso8601String(),
        'type': type,
        'initialStatus': 'PENDING_APPROVAL',
        'durationMinutes': 60,
        'notes': 'Demande du patient via application mobile',
      });
      
      return null; // Success
    } on DioException catch (e) {
      if (e.response?.statusCode == 409) {
        return 'Ce créneau est déjà réservé.';
      }
      return 'Erreur: ${e.message}';
    } catch (e) {
      return 'Erreur inattendue: $e';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: AppColors.backgroundGradient,
        ),
        child: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(24),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Mes Séances',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    GestureDetector(
                      onTap: _showRequestSeanceDialog,
                      child: Container(
                        width: 46,
                        height: 46,
                        decoration: BoxDecoration(
                          gradient: AppColors.primaryGradient,
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: const Icon(Icons.add, color: Colors.white),
                      ),
                    ),
                  ],
                ),
              ),
              
              // Calendar
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 24),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: TableCalendar(
                  firstDay: DateTime.utc(2020, 1, 1),
                  lastDay: DateTime.utc(2030, 12, 31),
                  focusedDay: _focusedDay,
                  calendarFormat: _calendarFormat,
                  selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
                  onDaySelected: (selectedDay, focusedDay) {
                    setState(() {
                      _selectedDay = selectedDay;
                      _focusedDay = focusedDay;
                    });
                  },
                  onFormatChanged: (format) {
                    setState(() {
                      _calendarFormat = format;
                    });
                  },
                  calendarStyle: CalendarStyle(
                    todayDecoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.5),
                      shape: BoxShape.circle,
                    ),
                    selectedDecoration: const BoxDecoration(
                      color: AppColors.accent,
                      shape: BoxShape.circle,
                    ),
                  ),
                  headerStyle: const HeaderStyle(
                    formatButtonVisible: true,
                    titleCentered: true,
                  ),
                ),
              ),
              const SizedBox(height: 24),
              
              // Seances list
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: const Text(
                  'Rendez-vous',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                ),
              ),
              const SizedBox(height: 16),
              
              Expanded(
                child: BlocBuilder<SeanceBloc, SeanceState>(
                  builder: (context, state) {
                    if (state is SeanceLoading) {
                      return const Center(child: CircularProgressIndicator());
                    }
                    
                    if (state is SeanceError) {
                      return Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(Icons.error_outline, size: 64, color: AppColors.error),
                            const SizedBox(height: 16),
                            Text(state.message),
                          ],
                        ),
                      );
                    }
                    
                    if (state is SeancesLoaded) {
                      if (state.seances.isEmpty) {
                        return Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.calendar_today, size: 64, color: AppColors.textMuted),
                              const SizedBox(height: 16),
                              Text('Aucune séance', style: TextStyle(color: AppColors.textSecondary)),
                            ],
                          ),
                        );
                      }
                      
                      return ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 24),
                        itemCount: state.seances.length,
                        itemBuilder: (context, index) {
                          return _SeanceCard(seance: state.seances[index]);
                        },
                      );
                    }
                    
                    return const SizedBox();
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SeanceCard extends StatelessWidget {
  final SeanceModel seance;

  const _SeanceCard({required this.seance});

  @override
  Widget build(BuildContext context) {
    Color statusColor;
    String statusText;
    
    switch (seance.status) {
      case 'COMPLETED':
        statusColor = AppColors.success;
        statusText = 'Terminée';
        break;
      case 'CONFIRMED':
        statusColor = AppColors.info;
        statusText = 'Confirmée';
        break;
      case 'CANCELLED':
        statusColor = AppColors.error;
        statusText = 'Annulée';
        break;
      default:
        statusColor = AppColors.warning;
        statusText = 'Planifiée';
    }

    return GestureDetector(
      onTap: () => _showSeanceDetails(context),
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                  decoration: BoxDecoration(
                    color: statusColor.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    statusText,
                    style: TextStyle(color: statusColor, fontSize: 11, fontWeight: FontWeight.w600),
                  ),
                ),
                const Spacer(),
                Icon(
                  seance.isVideoSession ? Icons.videocam : Icons.location_on,
                  size: 16,
                  color: AppColors.textMuted,
                ),
                const SizedBox(width: 4),
                Text(
                  seance.isVideoSession ? 'Vidéo' : 'Présentiel',
                  style: TextStyle(color: AppColors.textMuted, fontSize: 12),
                ),
                const SizedBox(width: 8),
                Icon(Icons.chevron_right, size: 18, color: AppColors.textMuted),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(Icons.calendar_today, size: 14, color: AppColors.accent),
                const SizedBox(width: 8),
                Text(
                  DateFormat('EEEE d MMMM', 'fr_FR').format(seance.scheduledAt),
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
                ),
                const SizedBox(width: 16),
                Icon(Icons.access_time, size: 14, color: AppColors.accent),
                const SizedBox(width: 8),
                Text(
                  DateFormat('HH:mm').format(seance.scheduledAt),
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showSeanceDetails(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) => Container(
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
        ),
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 20),
            Text(
              'Détails de la séance',
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),
            _DetailRow(
              icon: Icons.person,
              label: 'Thérapeute',
              value: seance.therapeuteName ?? 'Non assigné',
            ),
            const SizedBox(height: 12),
            _DetailRow(
              icon: Icons.calendar_today,
              label: 'Date',
              value: DateFormat('EEEE d MMMM yyyy', 'fr_FR').format(seance.scheduledAt),
            ),
            const SizedBox(height: 12),
            _DetailRow(
              icon: Icons.access_time,
              label: 'Heure',
              value: DateFormat('HH:mm').format(seance.scheduledAt),
            ),
            const SizedBox(height: 12),
            _DetailRow(
              icon: Icons.timer,
              label: 'Durée',
              value: '${seance.durationMinutes ?? 60} minutes',
            ),
            const SizedBox(height: 12),
            _DetailRow(
              icon: seance.isVideoSession ? Icons.videocam : Icons.location_on,
              label: 'Type',
              value: seance.isVideoSession ? 'Vidéo conférence' : 'En présentiel',
            ),
            if (seance.notes != null && seance.notes!.isNotEmpty) ...[
              const SizedBox(height: 16),
              const Divider(),
              const SizedBox(height: 12),
              Text('Notes', style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
              const SizedBox(height: 4),
              Text(seance.notes!, style: const TextStyle(fontSize: 14)),
            ],
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
                child: const Text('Fermer'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _DetailRow({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, size: 18, color: AppColors.primary),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: TextStyle(fontSize: 11, color: AppColors.textMuted)),
              Text(value, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
            ],
          ),
        ),
      ],
    );
  }
}

