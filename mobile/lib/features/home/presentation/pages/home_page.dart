import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../../appointments/presentation/bloc/seance_bloc.dart';
import '../../../appointments/presentation/bloc/seance_event.dart';
import '../../../appointments/presentation/bloc/seance_state.dart';
import '../../../appointments/data/models/seance_model.dart';
import '../bloc/home_bloc.dart';
import '../bloc/home_event.dart';
import '../bloc/home_state.dart';
import '../widgets/stat_card.dart';
import '../widgets/upcoming_appointment_card.dart';
import '../widgets/quick_action_button.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() async {
    final authState = context.read<AuthBloc>().state;
    if (authState is Authenticated) {
      // Get patient ID from user ID stored in SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      final userId = prefs.getInt('user_id') ?? 1;
      context.read<HomeBloc>().add(LoadPatientStats(userId, authState.token));
      context.read<SeanceBloc>().add(LoadUpcomingSeances(userId, authState.token));
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
          child: CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: _buildHeader(context),
              ),
              SliverToBoxAdapter(
                child: _buildStatsSection(),
              ),
              SliverToBoxAdapter(
                child: _buildQuickActions(context),
              ),
              SliverToBoxAdapter(
                child: _buildUpcomingAppointments(context),
              ),
              const SliverToBoxAdapter(
                child: SizedBox(height: 100),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return BlocBuilder<AuthBloc, AuthState>(
      builder: (context, state) {
        final username = state is Authenticated ? state.username : 'Utilisateur';
        final initial = username.isNotEmpty ? username.substring(0, 1).toUpperCase() : 'U';
        
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Row(
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  gradient: AppColors.accentGradient,
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.accent.withOpacity(0.3),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Center(
                  child: Text(
                    initial,
                    style: const TextStyle(
                      color: AppColors.background,
                      fontSize: 22,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _getGreeting(),
                      style: TextStyle(
                        color: AppColors.textMuted,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      username,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              IconButton(
                onPressed: () => context.go('/profile'),
                icon: const Icon(Icons.settings_outlined),
                color: AppColors.textPrimary,
              ),
            ],
          ),
        );
      },
    );
  }

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Bonjour 👋';
    if (hour < 18) return 'Bon après-midi ☀️';
    return 'Bonsoir 🌙';
  }

  Widget _buildStatsSection() {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeLoading) {
          return const Padding(
            padding: EdgeInsets.symmetric(horizontal: 24, vertical: 40),
            child: Center(
              child: CircularProgressIndicator(
                color: AppColors.primary,
              ),
            ),
          );
        }

        if (state is HomeError) {
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
            child: Text(
              'Erreur: ${state.message}',
              style: const TextStyle(color: AppColors.error),
              textAlign: TextAlign.center,
            ),
          );
        }

        final stats = state is HomeLoaded ? state.stats : null;
        
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Vue d\'ensemble',
                style: TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: StatCard(
                      title: 'Séances',
                      value: '${stats?.totalSeances ?? 0}',
                      subtitle: '${stats?.completedSeances ?? 0} terminées',
                      icon: Icons.calendar_today_rounded,
                      color: AppColors.primary,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: StatCard(
                      title: 'Progrès',
                      value: '${stats?.completionRate.toStringAsFixed(0) ?? 0}%',
                      subtitle: 'Taux de complétion',
                      icon: Icons.trending_up_rounded,
                      color: AppColors.success,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              if (stats?.riskScore != null)
                StatCard(
                  title: 'Score de Risque',
                  value: '${stats!.riskScore!.toStringAsFixed(0)}',
                  subtitle: stats.riskCategory ?? 'Non évalué',
                  icon: Icons.health_and_safety_rounded,
                  color: _getRiskColor(stats.riskScore!),
                ),
            ],
          ),
        );
      },
    );
  }

  Color _getRiskColor(double score) {
    if (score >= 70) return Colors.red;
    if (score >= 40) return Colors.orange;
    return AppColors.success;
  }

  Widget _buildQuickActions(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Actions rapides',
            style: TextStyle(
              color: AppColors.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: QuickActionButton(
                  icon: Icons.calendar_month_rounded,
                  label: 'Rendez-vous',
                  onTap: () => context.go('/appointments'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: QuickActionButton(
                  icon: Icons.analytics_rounded,
                  label: 'Prédictions',
                  onTap: () => context.go('/predictions'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildUpcomingAppointments(BuildContext context) {
    return BlocBuilder<SeanceBloc, SeanceState>(
      builder: (context, state) {
        if (state is SeanceLoading) {
          return const Padding(
            padding: EdgeInsets.all(24),
            child: Center(
              child: CircularProgressIndicator(
                color: AppColors.primary,
              ),
            ),
          );
        }

        if (state is SeanceError) {
          return Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              'Erreur: ${state.message}',
              style: const TextStyle(color: AppColors.error),
            ),
          );
        }

        final seances = state is SeancesLoaded ? state.seances : [];
        
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Prochains rendez-vous',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  TextButton(
                    onPressed: () => context.go('/appointments'),
                    child: const Text(
                      'Voir tout',
                      style: TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              if (seances.isEmpty)
                Center(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 20),
                    child: Text(
                      'Aucun rendez-vous à venir',
                      style: TextStyle(
                        color: AppColors.textMuted,
                        fontSize: 14,
                      ),
                    ),
                  ),
                )
              else
                ...seances.take(3).map((seance) => Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: UpcomingAppointmentCard(
                        therapeuteName: seance.therapeuteName ?? 'Thérapeute',
                        specialty: seance.type ?? 'Séance',
                        dateTime: seance.scheduledAt,
                        type: seance.type ?? 'Consultation',
                        imageUrl: '', // No image URL in API
                      ),
                    )),
            ],
          ),
        );
      },
    );
  }
}
