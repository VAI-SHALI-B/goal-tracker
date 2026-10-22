import os
from datetime import datetime, timezone
from flask import Flask, jsonify, request, render_template

app = Flask(__name__)

# Version identifier for CI/CD tracking
APP_VERSION = os.getenv("APP_VERSION", "v1")

# In-memory store for goals
goals_db = []
goal_id_counter = 1

def reset_db():
    """Helper used during testing to reset the database."""
    global goals_db, goal_id_counter
    goals_db = []
    goal_id_counter = 1

@app.route("/")
def home():
    """Renders the main Goal Tracker web dashboard."""
    return render_template("index.html", version=APP_VERSION, goals=goals_db)

@app.route("/health")
def health():
    """Health check endpoint for Docker & Kubernetes readiness/liveness probes."""
    return jsonify({
        "status": "healthy",
        "version": APP_VERSION,
        "app": "Goal Tracker",
        "student_id": "24MIS0378",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }), 200

@app.route("/api/goals", methods=["GET"])
def get_goals():
    """Returns all tracked goals."""
    return jsonify({
        "version": APP_VERSION,
        "count": len(goals_db),
        "goals": goals_db
    }), 200

@app.route("/api/goals", methods=["POST"])
def create_goal():
    """v1 Feature: Add - Create goal."""
    global goal_id_counter
    data = request.get_json(silent=True) or request.form.to_dict()

    if not data or not data.get("title", "").strip():
        return jsonify({
            "error": "Goal title is required"
        }), 400

    new_goal = {
        "id": goal_id_counter,
        "title": data.get("title").strip(),
        "description": data.get("description", "").strip(),
        "category": data.get("category", "General").strip() or "General",
        "target_date": data.get("target_date", "").strip() or "Not Specified",
        "status": "Pending",
        "created_at": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    }

    goals_db.append(new_goal)
    goal_id_counter += 1

    return jsonify({
        "message": "Goal created successfully",
        "version": APP_VERSION,
        "goal": new_goal
    }), 201

if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)
