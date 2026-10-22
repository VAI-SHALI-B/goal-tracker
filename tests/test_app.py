import pytest
from app import app, reset_db

@pytest.fixture(autouse=True)
def setup_teardown():
    reset_db()
    yield
    reset_db()

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

def test_health_check(client):
    """Test /health endpoint returns 200 and healthy status."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.get_json()
    assert data["status"] == "healthy"
    assert data["app"] == "Goal Tracker"
    assert data["student_id"] == "24MIS0378"

def test_home_page_rendering(client):
    """Test that main page renders successfully with roll number and title."""
    response = client.get("/")
    assert response.status_code == 200
    assert b"Goal Tracker" in response.data
    assert b"24MIS0378" in response.data

def test_get_goals_initially_empty(client):
    """Test getting goals initially returns empty list."""
    response = client.get("/api/goals")
    assert response.status_code == 200
    data = response.get_json()
    assert data["count"] == 0
    assert data["goals"] == []

def test_create_goal_success(client):
    """Test v1 feature: successfully create a new goal."""
    payload = {
        "title": "Complete Agile and DevOps Project",
        "description": "Implement v1, v2, and v3 with Jenkins and Kubernetes",
        "category": "Academic",
        "target_date": "2026-11-15"
    }
    response = client.post("/api/goals", json=payload)
    assert response.status_code == 201
    data = response.get_json()
    assert data["message"] == "Goal created successfully"
    assert data["goal"]["id"] == 1
    assert data["goal"]["title"] == "Complete Agile and DevOps Project"
    assert data["goal"]["category"] == "Academic"
    assert data["goal"]["status"] == "Pending"

def test_create_goal_missing_title(client):
    """Test creating a goal without title returns 400 error."""
    payload = {"description": "Missing title test"}
    response = client.post("/api/goals", json=payload)
    assert response.status_code == 400
    data = response.get_json()
    assert "error" in data

# ==================== v2 Tests ====================

def test_update_goal_success(client):
    """Test v2 feature: update goal details."""
    create_res = client.post("/api/goals", json={
        "title": "Learn Docker",
        "description": "Learn basic commands"
    })
    goal_id = create_res.get_json()["goal"]["id"]

    update_res = client.put(f"/api/goals/{goal_id}", json={
        "title": "Master Docker & Containers",
        "description": "Build multi-stage images",
        "category": "Career"
    })
    assert update_res.status_code == 200
    updated_goal = update_res.get_json()["goal"]
    assert updated_goal["title"] == "Master Docker & Containers"
    assert updated_goal["description"] == "Build multi-stage images"
    assert updated_goal["category"] == "Career"

def test_update_goal_status_and_progress(client):
    """Test v2 feature: update status and progress percentage."""
    create_res = client.post("/api/goals", json={"title": "Setup Jenkins"})
    goal_id = create_res.get_json()["goal"]["id"]

    client.put(f"/api/goals/{goal_id}", json={
        "status": "In Progress",
        "progress_pct": 50
    })
    get_res = client.get(f"/api/goals/{goal_id}")
    assert get_res.status_code == 200
    goal = get_res.get_json()["goal"]
    assert goal["status"] == "In Progress"
    assert goal["progress_pct"] == 50

    client.put(f"/api/goals/{goal_id}", json={
        "status": "Completed"
    })
    get_res2 = client.get(f"/api/goals/{goal_id}")
    assert get_res2.get_json()["goal"]["progress_pct"] == 100

def test_update_goal_not_found(client):
    """Test updating non-existent goal returns 404."""
    response = client.put("/api/goals/999", json={"title": "Non-existent"})
    assert response.status_code == 404
    assert "error" in response.get_json()

# ==================== v3 Tests ====================

def test_progress_endpoint_empty(client):
    """Test v3 feature: /api/progress with no goals returns 0s."""
    response = client.get("/api/progress")
    assert response.status_code == 200
    data = response.get_json()
    assert data["total_goals"] == 0
    assert data["overall_progress_pct"] == 0

def test_progress_endpoint_with_goals(client):
    """Test v3 feature: /api/progress accurately computes metrics and percentages."""
    # Goal 1: 100% completed
    client.post("/api/goals", json={"title": "Goal 1", "category": "Academic"})
    client.put("/api/goals/1", json={"status": "Completed", "progress_pct": 100})

    # Goal 2: 50% in progress
    client.post("/api/goals", json={"title": "Goal 2", "category": "Academic"})
    client.put("/api/goals/2", json={"status": "In Progress", "progress_pct": 50})

    # Goal 3: 0% pending
    client.post("/api/goals", json={"title": "Goal 3", "category": "Personal"})

    response = client.get("/api/progress")
    assert response.status_code == 200
    data = response.get_json()
    assert data["total_goals"] == 3
    assert data["completed_goals"] == 1
    assert data["in_progress_goals"] == 1
    assert data["pending_goals"] == 1
    # Average: (100 + 50 + 0) / 3 = 50.0%
    assert data["overall_progress_pct"] == 50.0
    assert "Academic" in data["category_summary"]
    assert data["category_summary"]["Academic"]["total"] == 2
    assert data["category_summary"]["Academic"]["completed"] == 1
