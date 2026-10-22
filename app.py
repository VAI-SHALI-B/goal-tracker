import os
from datetime import datetime, timezone
from flask import Flask, jsonify, request, render_template

app = Flask(__name__)

# Version identifier for CI/CD tracking (v1, v2, v3)
APP_VERSION = os.getenv("APP_VERSION", "v3")

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

@app.route("/api/goals/<int:goal_id>", methods=["GET"])
def get_single_goal(goal_id):
    """Returns a specific goal by ID."""
    goal = next((g for g in goals_db if g["id"] == goal_id), None)
    if not goal:
        return jsonify({"error": f"Goal with ID {goal_id} not found"}), 404
    return jsonify({"version": APP_VERSION, "goal": goal}), 200

@app.route("/api/goals", methods=["POST"])
def create_goal():
    """v1 Feature: Add - Create goal."""
    global goal_id_counter
    data = request.get_json(silent=True) or request.form.to_dict()

    if not data or not data.get("title", "").strip():
        return jsonify({"error": "Goal title is required"}), 400

    new_goal = {
        "id": goal_id_counter,
        "title": data.get("title").strip(),
        "description": data.get("description", "").strip(),
        "category": data.get("category", "General").strip() or "General",
        "target_date": data.get("target_date", "").strip() or "Not Specified",
        "status": "Pending",
        "progress_pct": int(data.get("progress_pct", 0)),
        "created_at": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S"),
        "updated_at": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    }

    goals_db.append(new_goal)
    goal_id_counter += 1

    return jsonify({
        "message": "Goal created successfully",
        "version": APP_VERSION,
        "goal": new_goal
    }), 201

@app.route("/api/goals/<int:goal_id>", methods=["PUT"])
def update_goal(goal_id):
    """v2 Feature: Update - Modify goal."""
    goal = next((g for g in goals_db if g["id"] == goal_id), None)
    if not goal:
        return jsonify({"error": f"Goal with ID {goal_id} not found"}), 404

    data = request.get_json(silent=True) or request.form.to_dict()
    if not data:
        return jsonify({"error": "No update data provided"}), 400

    # Modify permitted fields
    if "title" in data and data["title"].strip():
        goal["title"] = data["title"].strip()
    if "description" in data:
        goal["description"] = data["description"].strip()
    if "category" in data and data["category"].strip():
        goal["category"] = data["category"].strip()
    if "target_date" in data:
        goal["target_date"] = data["target_date"].strip()
    if "status" in data:
        valid_statuses = ["Pending", "In Progress", "Completed"]
        if data["status"] in valid_statuses:
            goal["status"] = data["status"]
            if data["status"] == "Completed":
                goal["progress_pct"] = 100
    if "progress_pct" in data:
        try:
            pct = max(0, min(100, int(data["progress_pct"])))
            goal["progress_pct"] = pct
            if pct == 100:
                goal["status"] = "Completed"
            elif pct > 0 and goal["status"] == "Pending":
                goal["status"] = "In Progress"
        except (ValueError, TypeError):
            pass

    goal["updated_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

    return jsonify({
        "message": "Goal updated successfully",
        "version": APP_VERSION,
        "goal": goal
    }), 200

@app.route("/api/progress", methods=["GET"])
def get_progress():
    """v3 Feature: View - Show progress."""
    total = len(goals_db)
    if total == 0:
        return jsonify({
            "version": APP_VERSION,
            "total_goals": 0,
            "completed_goals": 0,
            "in_progress_goals": 0,
            "pending_goals": 0,
            "overall_progress_pct": 0,
            "category_summary": {}
        }), 200

    completed = sum(1 for g in goals_db if g.get("status") == "Completed")
    in_progress = sum(1 for g in goals_db if g.get("status") == "In Progress")
    pending = sum(1 for g in goals_db if g.get("status") == "Pending")
    total_pct = sum(g.get("progress_pct", 0) for g in goals_db)
    overall_pct = round(total_pct / total, 1)

    categories = {}
    for g in goals_db:
        cat = g.get("category", "General")
        if cat not in categories:
            categories[cat] = {"total": 0, "completed": 0}
        categories[cat]["total"] += 1
        if g.get("status") == "Completed":
            categories[cat]["completed"] += 1

    return jsonify({
        "version": APP_VERSION,
        "total_goals": total,
        "completed_goals": completed,
        "in_progress_goals": in_progress,
        "pending_goals": pending,
        "overall_progress_pct": overall_pct,
        "category_summary": categories
    }), 200

if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)
