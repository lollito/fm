from playwright.sync_api import sync_playwright

def verify_register_page():
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        try:
            # Navigate to Register page
            print("Navigating to Register page...")
            page.goto("http://localhost:3000/register")

            # Wait for form to load
            print("Waiting for form...")
            page.wait_for_selector('form')

            # Verify accessible labels
            print("Verifying accessibility labels...")

            # Helper to check label-input association
            def check_label_input(label_text, input_id):
                label = page.get_by_text(label_text, exact=True)
                if label.count() > 1:
                     # Handle cases like "Email address" vs "Repeat Email address"
                     # Logic: find the specific label that matches exact text
                     pass

                # Check if label has 'for' attribute matching input 'id'
                # We can do this by evaluating JS
                is_associated = page.evaluate(f"""() => {{
                    const labels = Array.from(document.querySelectorAll('label'));
                    const targetLabel = labels.find(l => l.innerText.includes('{label_text}'));
                    if (!targetLabel) return false;
                    const htmlFor = targetLabel.getAttribute('for');
                    const input = document.getElementById('{input_id}');
                    return htmlFor === '{input_id}' && !!input;
                }}""")
                print(f"Label '{label_text}' associated with #{input_id}: {is_associated}")
                return is_associated

            # Check associations
            associations = [
                ('Username', 'username'),
                ('Email address', 'email'),
                ('Repeat Email address', 'emailConfirm'),
                ('Password', 'password'),
                ('Repeat Password', 'passwordConfirm'),
                ('Country', 'countryId'),
                ('Server', 'serverId'),
                ('Club Name', 'clubName')
            ]

            for label, input_id in associations:
                if not check_label_input(label, input_id):
                    print(f"FAILED: {label} not properly associated with {input_id}")

            # Fill form to trigger loading state
            print("Filling form...")
            page.fill('#username', 'testuser')
            page.fill('#email', 'test@test.com')
            page.fill('#emailConfirm', 'test@test.com')
            page.fill('#password', 'password123')
            page.fill('#passwordConfirm', 'password123')
            page.select_option('#countryId', index=1)
            page.select_option('#serverId', index=1)
            page.fill('#clubName', 'Test Club')

            # We need to mock the API response to be slow so we can capture the loading state
            # Playwright route interception
            print("Setting up network interception...")

            # Hold the request to keep loading state active
            def handle_route(route):
                print("Intercepted register request, holding...")
                # Do not fulfill immediately to keep spinner visible
                # We will wait a bit then fulfill or just take screenshot while pending
                pass

            # Intercept POST to /user/register (adjust path as needed based on api.js)
            # Assuming api.js uses relative paths or configured base URL.
            # If it's localhost:8080/user/register, we need to match that.
            # Let's match wildly to be safe
            page.route("**/user/register", lambda route: route.fulfill(status=200, body='{"token":"fake"}', delay=2000))

            # Click submit
            print("Submitting form...")
            page.click('button[type="submit"]')

            # Wait for loading state
            print("Waiting for loading state...")
            submit_btn = page.locator('button[type="submit"]')

            # Check for disabled attribute
            expect_disabled = submit_btn.is_disabled()
            print(f"Button disabled: {expect_disabled}")

            # Check for spinner class
            has_spinner = page.locator('.spinner-border').is_visible()
            print(f"Spinner visible: {has_spinner}")

            # Check for text change
            btn_text = submit_btn.inner_text()
            print(f"Button text: {btn_text}")

            # Take screenshot of loading state
            print("Taking screenshot...")
            page.screenshot(path="verification.png")
            print("Screenshot saved to verification.png")

        except Exception as e:
            print(f"Error: {e}")
            page.screenshot(path="error.png")
        finally:
            browser.close()

if __name__ == "__main__":
    verify_register_page()
