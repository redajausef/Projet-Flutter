# ML Prediction Service for ClinAssist
# Flask API with scikit-learn models

from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import numpy as np
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Model paths
MODEL_DIR = os.path.join(os.path.dirname(__file__), 'models', 'trained')
os.makedirs(MODEL_DIR, exist_ok=True)

# Load models (will be trained on first run if not exist)
dropout_model = None
progress_model = None
scheduler_model = None

def load_or_train_models():
    """Load pre-trained models or train new ones if not available"""
    global dropout_model, progress_model, scheduler_model
    
    dropout_path = os.path.join(MODEL_DIR, 'dropout_model.joblib')
    progress_path = os.path.join(MODEL_DIR, 'progress_model.joblib')
    scheduler_path = os.path.join(MODEL_DIR, 'scheduler_model.joblib')
    
    if os.path.exists(dropout_path):
        dropout_model = joblib.load(dropout_path)
        progress_model = joblib.load(progress_path)
        scheduler_model = joblib.load(scheduler_path)
        print("✓ Models loaded successfully")
    else:
        print("⚙ Training new models...")
        from training.train_models import train_all_models
        dropout_model, progress_model, scheduler_model = train_all_models(MODEL_DIR)
        print("✓ Models trained and saved")

@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'ClinAssist ML Prediction Service',
        'version': '1.0.0',
        'models_loaded': all([dropout_model, progress_model, scheduler_model]),
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/predict/dropout-risk', methods=['POST'])
def predict_dropout_risk():
    """
    Predict patient dropout risk
    
    Input Features:
    - cancellation_rate: float (0-1)
    - no_show_rate: float (0-1)
    - days_since_last_session: int
    - total_sessions: int
    - avg_mood_score: float (1-10)
    - age: int
    """
    try:
        data = request.json
        
        # Feature extraction
        features = np.array([[
            data.get('cancellation_rate', 0),
            data.get('no_show_rate', 0),
            data.get('days_since_last_session', 0),
            data.get('total_sessions', 0),
            data.get('avg_mood_score', 5),
            data.get('age', 30)
        ]])
        
        # Make prediction
        if dropout_model is None:
            # Fallback heuristic if model not loaded
            risk_score = calculate_heuristic_dropout_risk(data)
        else:
            risk_score = float(dropout_model.predict(features)[0])
            # Get probability for confidence
            if hasattr(dropout_model, 'predict_proba'):
                probas = dropout_model.predict_proba(features)[0]
                confidence = float(max(probas))
            else:
                confidence = 0.85
        
        # Determine risk category
        if risk_score < 25:
            risk_category = 'LOW'
        elif risk_score < 50:
            risk_category = 'MODERATE'
        elif risk_score < 75:
            risk_category = 'HIGH'
        else:
            risk_category = 'CRITICAL'
        
        return jsonify({
            'prediction_type': 'DROPOUT_RISK',
            'risk_score': round(risk_score, 2),
            'risk_category': risk_category,
            'confidence': round(confidence if 'confidence' in dir() else 0.85, 2),
            'factors': {
                'cancellation_impact': round(data.get('cancellation_rate', 0) * 30, 2),
                'no_show_impact': round(data.get('no_show_rate', 0) * 40, 2),
                'inactivity_impact': round(min(data.get('days_since_last_session', 0) / 30 * 30, 30), 2)
            },
            'model_version': '1.0.0',
            'algorithm': 'RandomForest'
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/predict/treatment-progress', methods=['POST'])
def predict_treatment_progress():
    """
    Predict treatment progress score
    
    Input Features:
    - total_sessions: int
    - avg_progress_rating: float (1-5)
    - mood_improvement: float (-10 to 10)
    - session_completion_rate: float (0-1)
    """
    try:
        data = request.json
        
        features = np.array([[
            data.get('total_sessions', 0),
            data.get('avg_progress_rating', 3),
            data.get('mood_improvement', 0),
            data.get('session_completion_rate', 0.8)
        ]])
        
        if progress_model is None:
            progress_score = calculate_heuristic_progress(data)
        else:
            progress_score = float(progress_model.predict(features)[0])
        
        # Ensure score is in valid range
        progress_score = max(0, min(100, progress_score))
        
        # Determine progress category
        if progress_score >= 80:
            category = 'EXCELLENT'
        elif progress_score >= 60:
            category = 'GOOD'
        elif progress_score >= 40:
            category = 'MODERATE'
        else:
            category = 'NEEDS_ATTENTION'
        
        return jsonify({
            'prediction_type': 'TREATMENT_PROGRESS',
            'progress_score': round(progress_score, 2),
            'progress_category': category,
            'confidence': 0.82,
            'recommendations': get_progress_recommendations(category),
            'model_version': '1.0.0',
            'algorithm': 'GradientBoosting'
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/predict/next-session', methods=['POST'])
def predict_next_session():
    """
    Predict optimal days until next session
    
    Input Features:
    - avg_days_between_sessions: float
    - current_risk_level: int (0-100)
    - last_progress_rating: int (1-5)
    - patient_age: int
    """
    try:
        data = request.json
        
        features = np.array([[
            data.get('avg_days_between_sessions', 7),
            data.get('current_risk_level', 25),
            data.get('last_progress_rating', 3),
            data.get('patient_age', 35)
        ]])
        
        if scheduler_model is None:
            recommended_days = calculate_heuristic_schedule(data)
        else:
            recommended_days = float(scheduler_model.predict(features)[0])
        
        # Ensure reasonable range (3-21 days)
        recommended_days = max(3, min(21, round(recommended_days)))
        
        # Adjust based on risk
        risk_level = data.get('current_risk_level', 25)
        if risk_level >= 70:
            recommended_days = min(recommended_days, 5)
            urgency = 'HIGH'
        elif risk_level >= 50:
            recommended_days = min(recommended_days, 7)
            urgency = 'MODERATE'
        else:
            urgency = 'NORMAL'
        
        return jsonify({
            'prediction_type': 'NEXT_SESSION',
            'recommended_days': recommended_days,
            'urgency': urgency,
            'confidence': 0.78,
            'reasoning': f"Based on patient history and current risk level ({risk_level}%)",
            'model_version': '1.0.0',
            'algorithm': 'LinearRegression'
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# Heuristic fallbacks
def calculate_heuristic_dropout_risk(data):
    risk = 0
    risk += data.get('cancellation_rate', 0) * 30
    risk += data.get('no_show_rate', 0) * 40
    risk += min(data.get('days_since_last_session', 0) / 30 * 30, 30)
    return min(100, risk)

def calculate_heuristic_progress(data):
    score = 50
    score += data.get('avg_progress_rating', 3) * 5
    score += data.get('mood_improvement', 0) * 10
    score += min(data.get('total_sessions', 0) * 2, 20)
    return max(0, min(100, score))

def calculate_heuristic_schedule(data):
    days = data.get('avg_days_between_sessions', 7)
    risk = data.get('current_risk_level', 25)
    if risk > 70:
        return max(3, days - 3)
    elif risk > 50:
        return max(5, days - 1)
    return days

def get_progress_recommendations(category):
    recommendations = {
        'EXCELLENT': "Excellente progression ! Envisagez une transition vers la phase de maintien.",
        'GOOD': "Bonne progression. Continuez l'approche thérapeutique actuelle.",
        'MODERATE': "Progression modérée. Envisagez d'ajuster l'intensité du traitement.",
        'NEEDS_ATTENTION': "La progression nécessite attention. Révision complète recommandée."
    }
    return recommendations.get(category, "Continuez le suivi de la progression.")

if __name__ == '__main__':
    load_or_train_models()
    app.run(host='0.0.0.0', port=5000, debug=False)
