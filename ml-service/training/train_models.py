# Training script for ML models
# Generates synthetic clinical data and trains scikit-learn models

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
import joblib
import os

def generate_synthetic_data(n_samples=1000, seed=42):
    """
    Generate realistic synthetic patient data for training
    Based on clinical patterns observed in therapy settings
    """
    np.random.seed(seed)
    
    # Patient demographics
    ages = np.random.randint(18, 75, n_samples)
    
    # Session history
    total_sessions = np.random.randint(1, 50, n_samples)
    
    # Cancellation patterns (higher for at-risk patients)
    base_cancellation = np.random.beta(2, 10, n_samples)  # Most patients have low cancellation
    
    # No-show patterns
    base_no_show = np.random.beta(1.5, 15, n_samples)
    
    # Days since last session (exponential distribution)
    days_since = np.random.exponential(14, n_samples).astype(int)
    days_since = np.clip(days_since, 1, 90)
    
    # Mood scores (1-10)
    avg_mood = np.random.normal(6, 2, n_samples)
    avg_mood = np.clip(avg_mood, 1, 10)
    
    # Progress ratings (1-5)
    progress_rating = np.random.normal(3.5, 0.8, n_samples)
    progress_rating = np.clip(progress_rating, 1, 5)
    
    # Mood improvement (-5 to 5)
    mood_improvement = np.random.normal(1, 2, n_samples)
    mood_improvement = np.clip(mood_improvement, -5, 5)
    
    # Session completion rate
    completion_rate = np.random.beta(8, 2, n_samples)
    
    # Average days between sessions
    avg_days_between = np.random.randint(5, 21, n_samples)
    
    # ===== Target Variables =====
    
    # Dropout risk score (0-100)
    # Higher with more cancellations, no-shows, and inactivity
    dropout_risk = (
        base_cancellation * 35 +
        base_no_show * 45 +
        np.minimum(days_since / 30 * 25, 25) +
        (10 - avg_mood) * 2 +
        np.random.normal(0, 5, n_samples)  # noise
    )
    dropout_risk = np.clip(dropout_risk, 0, 100)
    
    # Progress score (0-100)
    progress_score = (
        50 +
        progress_rating * 8 +
        mood_improvement * 6 +
        np.minimum(total_sessions * 0.5, 15) +
        completion_rate * 10 +
        np.random.normal(0, 5, n_samples)
    )
    progress_score = np.clip(progress_score, 0, 100)
    
    # Recommended days until next session (3-21)
    recommended_days = (
        avg_days_between * 0.7 +
        (100 - dropout_risk) * 0.05 +
        progress_rating * 0.5 +
        np.random.normal(0, 1, n_samples)
    )
    recommended_days = np.clip(recommended_days, 3, 21)
    
    # Create DataFrames
    dropout_features = pd.DataFrame({
        'cancellation_rate': base_cancellation,
        'no_show_rate': base_no_show,
        'days_since_last_session': days_since,
        'total_sessions': total_sessions,
        'avg_mood_score': avg_mood,
        'age': ages
    })
    
    progress_features = pd.DataFrame({
        'total_sessions': total_sessions,
        'avg_progress_rating': progress_rating,
        'mood_improvement': mood_improvement,
        'session_completion_rate': completion_rate
    })
    
    scheduler_features = pd.DataFrame({
        'avg_days_between_sessions': avg_days_between,
        'current_risk_level': dropout_risk,
        'last_progress_rating': progress_rating,
        'patient_age': ages
    })
    
    return {
        'dropout': (dropout_features, dropout_risk),
        'progress': (progress_features, progress_score),
        'scheduler': (scheduler_features, recommended_days)
    }

def train_dropout_model(X, y):
    """Train Random Forest for dropout risk prediction"""
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    model = RandomForestRegressor(
        n_estimators=100,
        max_depth=10,
        min_samples_split=5,
        random_state=42,
        n_jobs=-1
    )
    model.fit(X_train, y_train)
    
    score = model.score(X_test, y_test)
    print(f"  Dropout Risk Model R² Score: {score:.4f}")
    
    return model

def train_progress_model(X, y):
    """Train Gradient Boosting for treatment progress prediction"""
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    model = GradientBoostingRegressor(
        n_estimators=100,
        max_depth=5,
        learning_rate=0.1,
        random_state=42
    )
    model.fit(X_train, y_train)
    
    score = model.score(X_test, y_test)
    print(f"  Treatment Progress Model R² Score: {score:.4f}")
    
    return model

def train_scheduler_model(X, y):
    """Train Linear Regression for session scheduling"""
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    model = LinearRegression()
    model.fit(X_train, y_train)
    
    score = model.score(X_test, y_test)
    print(f"  Session Scheduler Model R² Score: {score:.4f}")
    
    return model

def train_all_models(save_dir):
    """Train all models and save them"""
    print("\n🔬 Generating synthetic training data...")
    data = generate_synthetic_data(n_samples=2000)
    
    print("🧠 Training ML models...")
    
    # Train Dropout Risk Model
    print("\n  Training Dropout Risk Model (Random Forest)...")
    X_dropout, y_dropout = data['dropout']
    dropout_model = train_dropout_model(X_dropout, y_dropout)
    
    # Train Progress Model
    print("  Training Treatment Progress Model (Gradient Boosting)...")
    X_progress, y_progress = data['progress']
    progress_model = train_progress_model(X_progress, y_progress)
    
    # Train Scheduler Model
    print("  Training Session Scheduler Model (Linear Regression)...")
    X_scheduler, y_scheduler = data['scheduler']
    scheduler_model = train_scheduler_model(X_scheduler, y_scheduler)
    
    # Save models
    print(f"\n💾 Saving models to {save_dir}...")
    os.makedirs(save_dir, exist_ok=True)
    
    joblib.dump(dropout_model, os.path.join(save_dir, 'dropout_model.joblib'))
    joblib.dump(progress_model, os.path.join(save_dir, 'progress_model.joblib'))
    joblib.dump(scheduler_model, os.path.join(save_dir, 'scheduler_model.joblib'))
    
    print("✅ All models trained and saved successfully!\n")
    
    return dropout_model, progress_model, scheduler_model

if __name__ == '__main__':
    # Train models when script is run directly
    save_dir = os.path.join(os.path.dirname(__file__), '..', 'models', 'trained')
    train_all_models(save_dir)
