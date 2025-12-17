class TherapeuteModel {
  final int id;
  final String firstName;
  final String lastName;
  final String? specialty;
  final String? bio;
  final double? rating;
  final int? yearsExperience;

  TherapeuteModel({
    required this.id,
    required this.firstName,
    required this.lastName,
    this.specialty,
    this.bio,
    this.rating,
    this.yearsExperience,
  });

  String get fullName => '$firstName $lastName';

  factory TherapeuteModel.fromJson(Map<String, dynamic> json) {
    return TherapeuteModel(
      id: json['id'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      specialty: json['specialty'] as String?,
      bio: json['bio'] as String?,
      rating: json['rating'] != null ? (json['rating'] as num).toDouble() : null,
      yearsExperience: json['yearsExperience'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'firstName': firstName,
      'lastName': lastName,
      'specialty': specialty,
      'bio': bio,
      'rating': rating,
      'yearsExperience': yearsExperience,
    };
  }
}
