from jakarta.inject import Singleton


@Singleton
class OrderService:

    def place(self, item: str) -> str:
        return f"placed {item}"

    def cancel(self, item: str) -> str:
        return f"cancelled {item}"
