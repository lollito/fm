import json
from playwright.sync_api import sync_playwright, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()

    # Mock user in localStorage
    user_data = {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "clubId": 1,
        "roles": ["ROLE_USER"],
        "accessToken": "fake-jwt-token"
    }

    # Define route mocks
    def handle_sponsorship_dashboard(route):
        print("Intercepted sponsorship dashboard request")
        route.fulfill(
            status=200,
            content_type="application/json",
            body=json.dumps({
                "activeDeals": [
                    {
                        "id": 1,
                        "sponsor": {
                            "id": 1,
                            "name": "TechGiant Corp",
                            "logo": "https://via.placeholder.com/150",
                            "industry": "Technology",
                            "tier": "PREMIUM"
                        },
                        "type": "SHIRT",
                        "currentAnnualValue": 2500000,
                        "totalValue": 7500000,
                        "startDate": "2023-07-01",
                        "endDate": "2026-06-30",
                        "status": "ACTIVE"
                    }
                ],
                "pendingOffers": [
                    {
                        "id": 2,
                        "sponsor": {
                            "id": 2,
                            "name": "Local Brews",
                            "logo": "https://via.placeholder.com/150",
                            "industry": "Beverage",
                            "tier": "STANDARD"
                        },
                        "type": "STADIUM",
                        "offeredAnnualValue": 1200000,
                        "contractYears": 5,
                        "leaguePositionBonus": 100000,
                        "cupProgressBonus": 50000,
                        "attendanceBonus": 0,
                        "status": "PENDING",
                        "offerDate": "2023-10-15",
                        "expiryDate": "2023-11-01",
                        "terms": "Standard stadium naming rights"
                    }
                ],
                "totalAnnualValue": 2500000,
                "totalActiveDeals": 1,
                "recentPayments": [],
                "clubAttractiveness": {
                    "leaguePositionScore": 15.0,
                    "stadiumScore": 65.0,
                    "financialScore": 40.0,
                    "historicalScore": 20.0,
                    "fanBaseScore": 30.0,
                    "overallScore": 34.0
                }
            })
        )

    def handle_financial_dashboard(route):
        print("Intercepted financial dashboard request")
        route.fulfill(
            status=200,
            content_type="application/json",
            body=json.dumps({
                "currentBalance": 1500000,
                "monthlyIncome": 200000,
                "monthlyExpenses": 180000,
                "netProfit": 20000,
                "totalRevenue": 2400000,
                "totalExpenses": 2160000,
                "recentTransactions": []
            })
        )

    # Add init script to set localStorage before page loads
    context.add_init_script(f"""
        localStorage.setItem('user', '{json.dumps(user_data)}');
    """)

    page = context.new_page()

    # Route interception
    page.route("**/api/sponsorship/club/1/dashboard", handle_sponsorship_dashboard)
    page.route("**/api/finance/club/1/dashboard", handle_financial_dashboard)

    try:
        print("Navigating to finance page...")
        page.goto("http://localhost:3000/finance")

        # Wait for tab to appear
        print("Waiting for tabs...")
        page.wait_for_selector(".nav-tabs")

        # Click Sponsorship tab
        print("Clicking Sponsorship tab...")
        page.click("text=Sponsorship")

        # Wait for dashboard to load
        print("Waiting for dashboard...")
        page.wait_for_selector(".sponsorship-dashboard")

        # Verify content
        expect(page.get_by_text("TechGiant Corp")).to_be_visible()
        expect(page.get_by_text("Local Brews")).to_be_visible()
        expect(page.get_by_text("Active Sponsorship Deals (1)")).to_be_visible()
        expect(page.get_by_text("Pending Offers (1)")).to_be_visible()

        # Take screenshot
        print("Taking screenshot...")
        page.screenshot(path="verification/sponsorship_dashboard.png", full_page=True)
        print("Screenshot saved to verification/sponsorship_dashboard.png")

    except Exception as e:
        print(f"Error: {e}")
        page.screenshot(path="verification/error.png")
        raise e
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
