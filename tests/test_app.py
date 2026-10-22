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

    # Verify it now appears in GET /api/goals
    get_res = client.get("/api/goals")
    assert get_res.status_code == 200
    assert get_res.get_json()["count"] == 1

def test_create_goal_missing_title(client):
    """Test creating a goal without title returns 400 error."""
    payload = {
        "description": "Missing title test"
    }
    response = client.post("/api/goals", json=payload)
    assert response.status_code == 400
    data = response.get_json()
    assert "error" in data
