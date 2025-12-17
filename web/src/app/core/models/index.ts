// ============================================
// ClinAssist - TypeScript Models
// ============================================

// User & Auth
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'THERAPEUTE' | 'RECEPTIONIST' | 'PATIENT';
  profileImageUrl?: string;
  enabled: boolean;
  createdAt: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// Patient
export interface Patient {
  id: number;
  patientCode: string;
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  dateOfBirth?: string;
  age?: number;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelation?: string;
  medicalHistory?: string;
  currentMedications?: string;
  allergies?: string;
  notes?: string;
  insuranceProvider?: string;
  insuranceNumber?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ON_HOLD' | 'DISCHARGED';
  assignedTherapeuteId?: number;
  assignedTherapeuteName?: string;
  riskScore?: number;
  riskCategory?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  totalSeances?: number;
  completedSeances?: number;
  nextSeanceAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  address?: string;
  city?: string;
  postalCode?: string;
  medicalHistory?: string;
  notes?: string;
  assignedTherapeuteId?: number;
}

// Therapeute
export interface Therapeute {
  id: number;
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  specialization: string;
  licenseNumber: string;
  yearsOfExperience?: number;
  bio?: string;
  available: boolean;
  totalPatients?: number;
  activePatients?: number;
  todaySeances?: number;
  weekSeances?: number;
  rating?: number;
  createdAt: string;
}

// Seance
export interface Seance {
  id: number;
  seanceCode: string;
  patientId: number;
  patientName: string;
  patientCode?: string;
  patientImageUrl?: string;
  therapeuteId: number;
  therapeuteName: string;
  therapeuteCode?: string;
  therapeuteImageUrl?: string;
  type: 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE_CALL' | 'HOME_VISIT' | 'GROUP_SESSION' | 'CONSULTATION' | 'THERAPY' | 'FOLLOW_UP' | 'VIDEO' | 'EMERGENCY';
  status: 'PENDING_APPROVAL' | 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'RESCHEDULED';
  scheduledAt: string;
  startedAt?: string;
  endedAt?: string;
  durationMinutes: number;
  notes?: string;
  objectives?: string;
  outcomes?: string;
  patientMood?: number;
  progress?: number;
  nextSteps?: string;
  isVideoSession: boolean;
  videoSessionUrl?: string;
  createdAt: string;
}

export interface SeanceCreateRequest {
  patientId: number;
  therapeuteId: number;
  type: 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE_CALL' | 'HOME_VISIT' | 'GROUP_SESSION';
  scheduledAt: string;
  durationMinutes: number;
  notes?: string;
  objectives?: string;
  isVideoSession?: boolean;
}

// Prediction
export interface Prediction {
  id: number;
  patientId: number;
  patientName: string;
  type: 'DROPOUT_RISK' | 'NEXT_SESSION' | 'TREATMENT_OUTCOME' | 'MOOD_TREND';
  score: number;
  confidence: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendation?: string;
  factors?: string[];
  generatedAt: string;
  validUntil?: string;
  isActive: boolean;
}

// Dashboard
export interface DashboardStats {
  totalPatients: number;
  activePatients: number;
  newPatientsThisMonth: number;
  patientGrowthPercentage: number;
  totalTherapeutes: number;
  availableTherapeutes: number;
  totalSeances: number;
  todaySeances: number;
  upcomingSeances: number;
  completedSeancesThisMonth: number;
  seanceCompletionRate: number;
  highRiskPatients: number;
  averageRiskScore: number;
  predictionAccuracy: number;
  totalPredictions: number;
  upcomingSeancesList: Seance[];
  recentPatients: Patient[];
  recentPredictions: Prediction[];
  seancesByType: { [key: string]: number };
  patientsByStatus: { [key: string]: number };
  seancesTrend: ChartDataPoint[];
  patientsTrend: ChartDataPoint[];
}

export interface ChartDataPoint {
  label: string;
  value: number;
  color?: string;
}

// Pagination
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Notification
export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: 'INFO' | 'WARNING' | 'ALERT' | 'REMINDER';
  read: boolean;
  actionUrl?: string;
  createdAt: string;
}

